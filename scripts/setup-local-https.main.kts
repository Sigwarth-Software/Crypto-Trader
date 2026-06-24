#!/usr/bin/env kotlin

import java.io.File
import kotlin.system.exitProcess

val repoRoot = File(".").absoluteFile.normalize()
val certDir = File(repoRoot, "certs")
val certFile = File(certDir, "localhost.pem")
val keyFile = File(certDir, "localhost-key.pem")
val rootCaFile = File(certDir, "rootCA.pem")

fun executableName(base: String): String {
    val osName = System.getProperty("os.name").lowercase()
    return if (osName.contains("windows")) "$base.exe" else base
}

fun hasCommand(command: String): Boolean =
    try {
        val process = ProcessBuilder(executableName(command), "--version")
            .redirectErrorStream(true)
            .start()
        process.waitFor()
        process.exitValue() == 0
    } catch (_: Exception) {
        false
    }

fun runCommand(vararg command: String) {
    val process = ProcessBuilder(*command)
        .directory(repoRoot)
        .inheritIO()
        .start()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        System.err.println("Command failed with exit code $exitCode: ${command.joinToString(" ")}")
        exitProcess(exitCode)
    }
}

fun captureCommand(vararg command: String): String {
    val process = ProcessBuilder(*command)
        .directory(repoRoot)
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText().trim()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        System.err.println("Command failed with exit code $exitCode: ${command.joinToString(" ")}")
        System.err.println(output)
        exitProcess(exitCode)
    }
    return output
}

fun asFileUri(file: File): String = file.toURI().toASCIIString()

fun copyMkcertRootCa() {
    val caRoot = captureCommand(executableName("mkcert"), "-CAROOT")
    val source = File(caRoot, "rootCA.pem")
    if (source.exists()) {
        source.copyTo(rootCaFile, overwrite = true)
    } else {
        System.err.println("mkcert root CA was not found at ${source.absolutePath}; CT_CA_BUNDLE will need to point at your trust store manually.")
    }
}

fun printNextSteps() {
    val certUri = asFileUri(certFile)
    val keyUri = asFileUri(keyFile)
    val rootCaPath = rootCaFile.absolutePath

    println(
        """
        
        Local HTTPS assets are ready:
          Certificate: ${certFile.absolutePath}
          Private key: ${keyFile.absolutePath}
          CA bundle: ${rootCaFile.absolutePath}
        
        Next, run the API with HTTPS enabled in the current PowerShell session:
          ${'$'}env:SERVER_SSL_ENABLED='true'
          ${'$'}env:SERVER_SSL_CERTIFICATE='$certUri'
          ${'$'}env:SERVER_SSL_CERTIFICATE_PRIVATE_KEY='$keyUri'
          ${'$'}env:SECURITY_JWT_ISSUER='https://localhost:8080'
          ${'$'}env:SECURITY_REFRESH_COOKIE_SECURE='true'
          ${'$'}env:CRYPTO_TRADER_ALLOWED_ORIGINS='https://localhost:4200'
          mvn -pl Crypto-Trader-Api -am spring-boot:run
        
        Run Data with HTTPS enabled:
          ${'$'}env:SERVER_SSL_ENABLED='true'
          ${'$'}env:SERVER_SSL_CERTIFICATE='$certUri'
          ${'$'}env:SERVER_SSL_CERTIFICATE_PRIVATE_KEY='$keyUri'
          ${'$'}env:CT_ANALYSIS_BASE_URL='https://localhost:8000'
          mvn -pl Crypto-Trader-Data -am spring-boot:run
        
        Run Analysis with HTTPS-aware service URLs:
          ${'$'}env:CT_API_BASE_URL='https://localhost:8080'
          ${'$'}env:CT_DATA_BASE_URL='https://localhost:8085'
          ${'$'}env:CT_CA_BUNDLE='$rootCaPath'
          ${'$'}env:CT_ANALYSIS_CERT_FILE='${certFile.absolutePath}'
          ${'$'}env:CT_ANALYSIS_KEY_FILE='${keyFile.absolutePath}'
          python Crypto-Trader-Analysis/scripts/run-local-https.py
        
        Then start the website from Crypto-Trader-Website:
          npm run start:https
        """.trimIndent()
    )
}

fun main() {
    if (!hasCommand("mkcert")) {
        System.err.println("mkcert is not installed or not on PATH. Install mkcert first, then rerun this script.")
        exitProcess(1)
    }

    certDir.mkdirs()

    println("Installing the local mkcert root CA if needed...")
    runCommand(executableName("mkcert"), "-install")

    println("Generating localhost certificate and key...")
    runCommand(
        executableName("mkcert"),
        "-key-file", keyFile.absolutePath,
        "-cert-file", certFile.absolutePath,
        "localhost",
        "127.0.0.1",
        "::1",
        "crypto-trader-analysis",
        "crypto-trader-data",
    )
    copyMkcertRootCa()

    printNextSteps()
}

main()
