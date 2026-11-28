#!/usr/bin/env kotlin
@file:DependsOn("org.jetbrains:annotations:26.0.1")

import java.io.File
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

// TODO: Clean up code.

// Run from the repository root. Requires JDK 23, Maven, Docker, and the same
// application configuration/AWS access used by .aws/buildspec-docs.yml.
// The docs Spring profile uses an in-memory H2 database.
val usage = """
Usage: kotlin scripts/generate-openapi.main.kts [--skip-build]

Build API/Data, start Kafka and the docs-profile services, export both OpenAPI
specs, and stop the API/Data processes on success, failure, or interruption.
  --skip-build  reuse existing service JARs
  --help        print this help without running commands

Output: Crypto-Trader-Docs/docs/api/openapi-{api,data}.yaml
Logs:   target/openapi-local/{api,data}.log
Kafka is left running; no containers or volumes are removed.
""".trimIndent()
if (args.any { it == "--help" || it == "-h" }) {
    println(usage)
    exitProcess(0)
}
require(args.all { it == "--skip-build" }) { usage }

val root = File(".").canonicalFile
require(File(root, ".aws/buildspec-docs.yml").isFile) { "Run from the Crypto-Trader repository root." }
val windows = System.getProperty("os.name").startsWith("Windows")
val java = File(System.getProperty("java.home"), "bin/${if (windows) "java.exe" else "java"}").absolutePath
val logs = File(root, "target/openapi-local")
val specs = File(root, "Crypto-Trader-Docs/docs/api")
val services = CopyOnWriteArrayList<Process>()

fun command(vararg arguments: String, capture: Boolean = false): String {
    val argv = arguments.toList()
    val executable = if (windows && argv.first() == "mvn") listOf("cmd.exe", "/c") + argv else argv
    println("> ${argv.joinToString(" ")}")
    val builder = ProcessBuilder(executable).directory(root)
    if (capture) builder.redirectError(ProcessBuilder.Redirect.INHERIT) else builder.inheritIO()
    val process = builder.start()
    val output = if (capture) process.inputStream.bufferedReader().use { it.readText() } else ""
    check(process.waitFor() == 0) { "Command failed: ${argv.joinToString(" ")}" }
    return output.replace(Regex("\u001B\\[[;\\d]*m"), "").trim()
}

fun portOpen(port: Int): Boolean = try {
    Socket().use { it.connect(InetSocketAddress("127.0.0.1", port), 1000) }
    true
} catch (_: Exception) { false }

fun stopServices() {
    services.forEach { process ->
        if (process.isAlive) {
            process.destroy()
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                process.waitFor(5, TimeUnit.SECONDS)
            }
        }
    }
}
Runtime.getRuntime().addShutdownHook(Thread { stopServices() })

fun findJar(module: String): File {
    val jars = File(root, "$module/target").listFiles().orEmpty().filter {
        it.isFile && it.extension == "jar" &&
            listOf("-sources", "-javadoc", "-tests").none { suffix -> it.name.contains(suffix) }
    }
    check(jars.size == 1) { "Expected one service JAR in $module/target; found ${jars.size}. Rebuild or remove stale artifacts." }
    return jars.single()
}

fun startService(name: String, module: String, port: Int, instrument: File): Process {
    val arguments = buildList {
        addAll(
            listOf(java, "-Xms512m", "-Xmx4g", "-XX:MetaspaceSize=256m",
                "-XX:MaxMetaspaceSize=1g", "-XX:+UseG1GC")
        )
        addAll(if (name == "data") listOf("-Dcryptotrader.load.currency=false") else emptyList())
        addAll(
            listOf("-javaagent:${instrument.absolutePath}", "-jar", findJar(module).absolutePath,
                "--spring.profiles.active=docs", "--server.port=$port", "--server.ssl.enabled=false",
                "--spring.config.import=optional:aws-secretsmanager:/cryptotrader/dev",
                "--cryptotrader.harvest.currency=false")
        )
    }
    val process = ProcessBuilder(arguments).directory(root).redirectErrorStream(true)
        .redirectOutput(File(logs, "$name.log")).start()
    services.add(process)
    println("Started $name on :$port; log: ${File(logs, "$name.log")}")
    return process
}

fun fetchSpec(port: Int): ByteArray? {
    for (path in listOf("v3/api-docs.yaml", "v3/api-docs")) {
        val connection = URI("http://127.0.0.1:$port/$path").toURL().openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 2000
            connection.readTimeout = 5000
            if (connection.responseCode != 200) continue
            val body = connection.inputStream.use { it.readBytes() }
            val text = body.toString(Charsets.UTF_8)
            if (body.size > 512 && Regex("(?m)^openapi\\s*:|\"openapi\"\\s*:").containsMatchIn(text)) return body
        } catch (_: Exception) {
            // A service may still be starting. Retry until the deadline.
        } finally {
            connection.disconnect()
        }
    }
    return null
}

fun saveSpec(name: String, bytes: ByteArray) {
    val target = File(specs, "openapi-$name.yaml").toPath()
    val temporary = Files.createTempFile(specs.toPath(), "openapi-$name-", ".tmp")
    try {
        Files.write(temporary, bytes)
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
    } finally {
        Files.deleteIfExists(temporary)
    }
    println("Saved $target (${bytes.size} bytes)")
}

try {
    for (port in listOf(18080, 18081)) {
        check(!portOpen(port)) { "Port $port is already in use; stop that service before running this script." }
    }
    check(logs.isDirectory || logs.mkdirs()) { "Cannot create $logs" }
    check(specs.isDirectory || specs.mkdirs()) { "Cannot create $specs" }

    command("docker", "compose", "-f", "infra/kafka/docker-compose.yml", "up", "-d")
    val kafkaDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(120)
    while (!portOpen(9092)) {
        check(System.nanoTime() < kafkaDeadline) { "Kafka did not open port 9092 within 120 seconds." }
        Thread.sleep(2000)
    }
    if ("--skip-build" !in args) command("mvn", "-B", "-DskipTests", "-am", "-U", "-P", "release", "install")

    val version = command("mvn", "-q", "help:evaluate", "-Dexpression=spring-framework.version", "-DforceStdout", capture = true)
    check(version.matches(Regex("[0-9][A-Za-z0-9.\\-]*"))) { "Unexpected Spring version output: $version" }
    command("mvn", "-q", "dependency:get", "-Dartifact=org.springframework:spring-instrument:$version")
    val repository = command("mvn", "-q", "help:evaluate", "-Dexpression=settings.localRepository", "-DforceStdout", capture = true)
    val instrument = File(repository, "org/springframework/spring-instrument/$version/spring-instrument-$version.jar")
    check(instrument.isFile) { "Spring instrumentation agent not found: $instrument" }

    val running = listOf(
        Triple("api", 18080, startService("api", "Crypto-Trader-Api", 18080, instrument)),
        Triple("data", 18081, startService("data", "Crypto-Trader-Data", 18081, instrument))
    )
    val pending = running.toMutableList()
    val deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(8)
    println("Waiting up to eight minutes for both OpenAPI endpoints...")
    while (pending.isNotEmpty()) {
        running.forEach { (name, _, process) -> check(process.isAlive) { "$name exited; inspect ${File(logs, "$name.log")}" } }
        check(System.nanoTime() < deadline) { "Timed out waiting for ${pending.map { it.first }}; inspect $logs" }
        pending.removeAll { (name, port, _) ->
            val body = fetchSpec(port)
            if (body != null) saveSpec(name, body)
            body != null
        }
        if (pending.isNotEmpty()) Thread.sleep(2000)
    }
    // The Docs module fetches/stages the same specs and module references as CI.
    command("mvn", "-B", "-DskipTests", "-Ddocs.skipOpenapi=false", "-f", "Crypto-Trader-Docs/pom.xml", "-N", "verify")
    println("OpenAPI specs ready in $specs. Refresh MkDocs or rebuild the site.")
} catch (exception: Exception) {
    System.err.println("OpenAPI generation failed: ${exception.message}")
    exitProcess(1) // The shutdown hook stops any services already started.
} finally {
    stopServices()
}
