#!/usr/bin/env kotlin

import java.io.File
import kotlin.system.exitProcess

// TODO: Clean up code.

/** Generate coverage documentation using steps 8–10 of .aws/buildspec-docs.yml. */
val supportedOptions = setOf("--skip-tests", "--skip-site-build", "--refresh-dependencies")
if ("--help" in args || "-h" in args) {
    println("""
        Usage: kotlin scripts/generate-coverage.main.kts [options]
        Run from the Crypto-Trader repository root.

          --skip-tests             reuse the existing aggregate JaCoCo XML
          --skip-site-build        generate docs/coverage only (for mkdocs serve)
          --refresh-dependencies   force Maven dependency update checks

        Requires Kotlin, JDK 23+, Maven, Python, .NET and ReportGenerator on PATH.
        Install ReportGenerator: dotnet tool install --global dotnet-reportgenerator-globaltool
        A site rebuild also requires mkdocs and mkdocs-material on PATH.
        Tests use the same coverage profile and exclusions as CI; supply CI-equivalent
        test configuration. No application services or Docker containers are started.
        Set DOCS_KOTLIN_INCREMENTAL=false to disable incremental Kotlin compilation.

        Output: Crypto-Trader-Docs/docs/coverage/index.html
                Crypto-Trader-Docs/site/coverage/index.html (unless --skip-site-build)
    """.trimIndent())
    exitProcess(0)
}

val root = File(".").absoluteFile.normalize()
val docsDir = File(root, "Crypto-Trader-Docs")
val report = File(root, "Crypto-Trader-Coverage/target/site/jacoco-aggregate/jacoco.xml")
val output = File(docsDir, "docs/coverage")
val isWindows = System.getProperty("os.name").lowercase().contains("windows")

fun runCommand(directory: File, command: List<String>) {
    println("\n> ${command.joinToString(" ")}")
    val wrapped = if (isWindows && command.first() == "mvn") listOf("cmd.exe", "/c") + command else command
    val result = ProcessBuilder(wrapped).directory(directory).inheritIO().start().waitFor()
    check(result == 0) { "Command failed with exit code $result: ${command.joinToString(" ")}" }
}

try {
    require(args.all { it in supportedOptions }) { "Unknown option. Use --help for supported options." }
    check(File(root, "pom.xml").isFile && File(docsDir, "mkdocs.yml").isFile) {
        "Run this script from the Crypto-Trader repository root."
    }

    if ("--skip-tests" !in args) {
        runCommand(root, listOf(if (isWindows) "python" else "python3", "scripts/check-coverage-modules.py"))
        val incremental = System.getenv("DOCS_KOTLIN_INCREMENTAL")?.toBooleanStrictOrNull() ?: true
        val command = listOf("mvn", "-B", "-ntp", "-T1", "-Dkotlin.compiler.incremental=$incremental",
            "-Ddokka.skip=true", "-Pcoverage", "-DskipITs=true", "-DfailIfNoTests=false",
            "-Djunit.platform.excludeTags=ui") +
            (if ("--refresh-dependencies" in args) listOf("-U") else emptyList()) + "verify"
        runCommand(root, command)
    }

    check(report.isFile && report.length() > 0) {
        "Missing or empty ${report.path}. Run without --skip-tests to generate aggregate coverage."
    }
    val html = File(output, "index.html")
    // Remove the previous summary so a failed generation cannot pass validation.
    check(!html.exists() || html.delete()) { "Cannot replace ${html.path}." }
    runCommand(root, listOf("reportgenerator",
        "-reports:${report.absolutePath}", "-targetdir:${output.absolutePath}",
        "-reporttypes:Html;HtmlChart;Badges", "-sourcedirs:."))

    // Older ReportGenerator versions use index.htm; MkDocs links to index.html.
    val htm = File(output, "index.htm")
    if (!html.exists() && htm.isFile && htm.length() > 0) htm.copyTo(html)
    check(html.isFile && html.length() > 0) { "ReportGenerator did not produce ${html.path}." }

    if ("--skip-site-build" !in args) {
        runCommand(docsDir, listOf("mkdocs", "build", "--site-dir", "site"))
        val siteIndex = File(docsDir, "site/coverage/index.html")
        check(siteIndex.isFile && siteIndex.length() > 0) { "MkDocs did not publish ${siteIndex.path}." }
        println("\nCoverage site ready: ${siteIndex.absolutePath}")
    }
    println("\nCoverage documentation ready: ${html.absolutePath}")
} catch (exception: Exception) {
    System.err.println("\nCoverage build FAILED: ${exception.message}")
    exitProcess(1)
}
