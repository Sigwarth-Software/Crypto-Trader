#!/usr/bin/env kotlin

import java.io.File
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

// TODO: Clean up code.

/**
 * Local Crypto-Trader Docs Build
 *
 * Reproduces the steps of `.aws/buildspec-docs.yml` on this machine and stops before the
 * S3 upload and the GitHub Pages deployment. The result is the static MkDocs site in
 * `Crypto-Trader-Docs/site`, which can be opened directly in a browser.
 *
 * Usage (from the repository root):
 *   kotlin scripts/generate-docs.main.kts
 *   kotlin scripts/generate-docs.main.kts --skip-openapi
 *   kotlin scripts/generate-docs.main.kts --skip-openapi --skip-coverage
 *
 * Options:
 *   --skip-openapi   skip Kafka, the API/Data services and the OpenAPI spec generation
 *   --skip-coverage  skip the test/coverage run and the coverage HTML report
 *   --external-kafka use KAFKA_BOOTSTRAP_SERVERS without starting/stopping Docker
 *   --refresh-dependencies force Maven to check for updated dependencies
 */

if ("--help" in args || "-h" in args) {
    println(
        """
        Usage: kotlin scripts/generate-docs.main.kts [--skip-openapi] [--skip-coverage] [--external-kafka] [--refresh-dependencies]
          --skip-openapi   skip Kafka, the API/Data services and the OpenAPI spec generation
          --skip-coverage  skip the test/coverage run and the coverage HTML report
          --external-kafka use an existing broker from KAFKA_BOOTSTRAP_SERVERS (default localhost:9092)
          --refresh-dependencies force Maven dependency update checks (normally use its cache policy)
        Set DOCS_KOTLIN_INCREMENTAL=false to disable incremental Kotlin compilation.
        """.trimIndent()
    )
    exitProcess(0)
}

val skipOpenapi = "--skip-openapi" in args
val skipCoverage = "--skip-coverage" in args
val externalKafka = "--external-kafka" in args
val refreshDependencies = "--refresh-dependencies" in args
val incrementalKotlin = System.getenv("DOCS_KOTLIN_INCREMENTAL")?.toBooleanStrictOrNull() ?: true
val incrementalCompilerOption = "-Dkotlin.compiler.incremental=$incrementalKotlin"
require(args.all { it in setOf("--skip-openapi", "--skip-coverage", "--external-kafka", "--refresh-dependencies") }) {
    "Unknown option. Use --help to see supported options."
}
val kafkaBrokers = System.getenv("KAFKA_BOOTSTRAP_SERVERS")?.takeIf { it.isNotBlank() } ?: "localhost:9092"
val firstBroker = URI("tcp://${kafkaBrokers.substringBefore(',')}")
require(firstBroker.host != null && firstBroker.port in 1..65535) { "KAFKA_BOOTSTRAP_SERVERS must contain host:port addresses" }

val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val root = File(".").absoluteFile.normalize()
val docsDir = File(root, "Crypto-Trader-Docs")
val specDir = File(docsDir, "docs/api")
val siteDir = File(docsDir, "site")
val logDir = File(root, "target/docs-local")
val kafkaCompose = "infra/kafka/docker-compose.yml"
val minSpecBytes = 512
val servicePorts = mapOf("api" to 18080, "data" to 18081)
val serviceJvmOpts = listOf("-Xms512m", "-Xmx4g", "-XX:MetaspaceSize=256m", "-XX:MaxMetaspaceSize=1g", "-XX:+UseG1GC")
val startedServices = mutableListOf<Pair<String, Process>>()

if (!File(root, "pom.xml").isFile || !docsDir.isDirectory) {
    System.err.println("Run this script from the Crypto-Trader repository root.")
    exitProcess(1)
}

fun wrap(command: List<String>): List<String> =
    if (isWindows && command.first() in listOf("mvn", "npm", "kotlin")) listOf("cmd.exe", "/c") + command else command

fun runCommand(dir: File, vararg command: String) {
    val commandList = command.toList()
    println("\n> ${commandList.joinToString(" ")}   [${dir.name}]")
    val result = ProcessBuilder(wrap(commandList)).directory(dir).inheritIO().start().waitFor()
    check(result == 0) { "Command failed with exit code $result: ${commandList.joinToString(" ")}" }
}

fun captureCommand(dir: File, vararg command: String): String {
    val commandList = command.toList()
    val process = ProcessBuilder(wrap(commandList)).directory(dir)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
    val output = process.inputStream.bufferedReader().readText().trim()
    check(process.waitFor() == 0) { "Command failed: ${commandList.joinToString(" ")}" }
    return output
}

fun portOpen(port: Int, host: String = "127.0.0.1"): Boolean = try {
    Socket().use { it.connect(InetSocketAddress(host, port), 1000) }
    true
} catch (exception: Exception) {
    false
}

fun waitForPort(port: Int, seconds: Int, host: String = "127.0.0.1") {
    val deadline = System.currentTimeMillis() + seconds * 1000L
    while (System.currentTimeMillis() < deadline) {
        if (portOpen(port, host)) return
        Thread.sleep(2000)
    }
    error("$host:$port did not open within $seconds seconds")
}

fun findJar(module: String): File {
    val jar = File(root, "$module/target").listFiles()
        ?.firstOrNull { file ->
            file.isFile && file.name.endsWith(".jar") &&
                listOf("-sources", "-javadoc", "-tests").none { file.name.contains(it) }
        }
    check(jar != null) { "No runnable jar found in $module/target. Build step 2 must succeed first." }
    return jar
}

fun startService(name: String, jar: File, port: Int, springInstrument: File, extraJvmOpts: List<String>) {
    logDir.mkdirs()
    val command = listOf("java") + serviceJvmOpts + extraJvmOpts + listOf(
        "-javaagent:${springInstrument.absolutePath}",
        "-jar", jar.absolutePath,
        "--spring.profiles.active=docs",
        "--server.port=$port",
        "--server.ssl.enabled=false",
        "--spring.kafka.bootstrap-servers=$kafkaBrokers",
        "--spring.cloud.stream.kafka.binder.brokers=$kafkaBrokers",
        "--spring.config.import=optional:aws-secretsmanager:/cryptotrader/dev",
        "--cryptotrader.harvest.currency=false"
    )
    val process = ProcessBuilder(command)
        .directory(root)
        .redirectErrorStream(true)
        .redirectOutput(File(logDir, "$name.log"))
        .start()
    startedServices += name to process
    println("Started $name on :$port (log: ${File(logDir, "$name.log").path})")
}

fun stopServices() {
    startedServices.forEach { (name, process) ->
        if (process.isAlive) {
            println("Stopping $name")
            process.destroy()
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                process.waitFor(5, TimeUnit.SECONDS)
            }
        }
    }
    startedServices.clear()
}

fun fetchSpec(port: Int, path: String): ByteArray? = try {
    val connection = URI("http://localhost:$port/$path").toURL().openConnection() as HttpURLConnection
    connection.connectTimeout = 3000
    connection.readTimeout = 10000
    if (connection.responseCode == 200) connection.inputStream.use { it.readBytes() } else null
} catch (exception: Exception) {
    null
}

fun isSpecReady(port: Int): Boolean = (fetchSpec(port, "v3/api-docs.yaml")?.size ?: 0) > minSpecBytes

fun waitForServices() {
    val deadline = System.currentTimeMillis() + 8 * 60_000L
    while (System.currentTimeMillis() < deadline) {
        startedServices.forEach { (name, process) ->
            check(process.isAlive) { "$name exited before readiness. Inspect ${File(logDir, "$name.log")}" }
        }
        if (servicePorts.values.all { isSpecReady(it) }) return
        Thread.sleep(2000)
    }
    error("Timed out waiting for OpenAPI endpoints. Inspect ${logDir.path}")
}

fun saveSpecIfMissing(name: String, port: Int) {
    val target = File(specDir, "openapi-$name.yaml")
    if (target.length() > 0) {
        println("Found existing spec for $name at ${target.path}")
        return
    }
    val body = fetchSpec(port, "v3/api-docs.yaml") ?: fetchSpec(port, "v3/api-docs")
    if (body == null || body.isEmpty()) {
        println("WARNING: could not fetch the $name spec from :$port; the API reference will be missing")
        return
    }
    target.writeBytes(body)
    println("Saved ${target.path} from :$port")
}

var exitCode = 0
var kafkaStarted = false

try {
    println("=== 1. Website (Compodoc) ===")
    val website = File(root, "Crypto-Trader-Website")
    runCommand(website, "npm", "install", "--prefer-offline", "--no-audit", "--no-fund")
    runCommand(website, "npm", "run", "docs:generate")

    println("\n=== 2. Dokka (all modules) ===")
    // Docs analysis is memory-heavy: override the repository's thread-per-core default.
    // The release profile runs Dokka at verify, so install already generates the docs.
    val buildArguments = listOf("mvn", "-B", "-e", "-T1", "-DskipTests", incrementalCompilerOption, "-am") +
        (if (refreshDependencies) listOf("-U") else emptyList()) + listOf("-P", "release", "install")
    runCommand(root, *buildArguments.toTypedArray())

    if (!skipOpenapi) {
        println("\n=== 3. Kafka + API/Data services ===")
        if (!externalKafka) {
            runCommand(root, "docker", "compose", "-f", kafkaCompose, "up", "-d")
            kafkaStarted = true
        }
        waitForPort(firstBroker.port, 120, firstBroker.host)

        val springVersion = captureCommand(root, "mvn", "-q", "help:evaluate",
            "-Dexpression=spring-framework.version", "-DforceStdout")
        runCommand(root, "mvn", "-q", "dependency:get",
            "-Dartifact=org.springframework:spring-instrument:$springVersion")
        val springInstrument = File(System.getProperty("user.home"),
            ".m2/repository/org/springframework/spring-instrument/$springVersion/spring-instrument-$springVersion.jar")
        check(springInstrument.isFile) { "Spring instrumentation agent not found: ${springInstrument.path}" }

        startService("api", findJar("Crypto-Trader-Api"), servicePorts.getValue("api"), springInstrument, emptyList())
        startService("data", findJar("Crypto-Trader-Data"), servicePorts.getValue("data"), springInstrument,
            listOf("-Dcryptotrader.load.currency=false"))
        waitForServices()

        println("\n=== 4. OpenAPI specs ===")
        runCommand(root, "mvn", "-B", "-T1", "-DskipTests", "-Ddokka.skip=true", "-Ddocs.skipOpenapi=false", "-f", "Crypto-Trader-Docs/pom.xml", "-N", "verify")
        specDir.mkdirs()
        servicePorts.forEach { (name, port) -> saveSpecIfMissing(name, port) }
        stopServices()
    } else {
        // Stage references after the entire reactor has generated them.
        runCommand(root, "mvn", "-B", "-T1", "-DskipTests", "-Ddokka.skip=true", "-Ddocs.skipOpenapi=true",
            "-f", "Crypto-Trader-Docs/pom.xml", "-N", "verify")
    }

    if (!skipCoverage) {
        println("\n=== 5. Tests + coverage ===")
        val coverageCommand = listOf("kotlin", "scripts/generate-coverage.main.kts", "--skip-site-build") +
            (if (refreshDependencies) listOf("--refresh-dependencies") else emptyList())
        runCommand(root, *coverageCommand.toTypedArray())
    }

    println("\n=== 7. MkDocs site ===")
    runCommand(docsDir, "mkdocs", "build", "--site-dir", "site")
    val siteApiDir = File(siteDir, "api")
    siteApiDir.mkdirs()
    specDir.listFiles { file -> file.name.startsWith("openapi-") && file.name.endsWith(".yaml") }
        ?.forEach { it.copyTo(File(siteApiDir, it.name), overwrite = true) }
    File(siteDir, ".nojekyll").createNewFile()

    val indexFile = File(siteDir, "index.html")
    check(indexFile.isFile) { "Site build incomplete: ${indexFile.path} is missing" }
    println("\nLocal docs ready: ${indexFile.absolutePath}")
    println("Live preview with hot reload: cd Crypto-Trader-Docs; mkdocs serve")
} catch (exception: Exception) {
    System.err.println("\nDocs build FAILED: ${exception.message}")
    exitCode = 1
} finally {
    stopServices()
    if (kafkaStarted) {
        runCatching { runCommand(root, "docker", "compose", "-f", kafkaCompose, "down", "-v") }
            .onFailure { System.err.println("Could not stop Kafka: ${it.message}") }
    }
}

exitProcess(exitCode)
