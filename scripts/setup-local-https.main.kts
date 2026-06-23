#!/usr/bin/env kotlin

import java.io.File
import kotlin.system.exitProcess

val repoRoot = File(".").absoluteFile.normalize()
val websiteCertDir = File(repoRoot, "Crypto-Trader-Website/certs")
val certFile = File(websiteCertDir, "localhost.pem")
val keyFile = File(websiteCertDir, "localhost-key.pem")

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

fun asFileUri(file: File): String = file.toURI().toASCIIString()

fun printNextSteps() {
    val certUri = asFileUri(certFile)
    val keyUri = asFileUri(keyFile)

    println(
        """
        
        Local HTTPS assets are ready:
          Certificate: ${certFile.absolutePath}
          Private key: ${keyFile.absolutePath}
        
        Next, run the API with HTTPS enabled in the current PowerShell session:
          ${'$'}env:SERVER_SSL_ENABLED='true'
          ${'$'}env:SERVER_SSL_CERTIFICATE='$certUri'
          ${'$'}env:SERVER_SSL_CERTIFICATE_PRIVATE_KEY='$keyUri'
          ${'$'}env:SECURITY_JWT_ISSUER='https://localhost:8080'
          ${'$'}env:SECURITY_REFRESH_COOKIE_SECURE='true'
          ${'$'}env:CRYPTO_TRADER_ALLOWED_ORIGINS='https://localhost:4200'
          mvn -pl Crypto-Trader-Api -am spring-boot:run
        
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

    websiteCertDir.mkdirs()

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
    )

    printNextSteps()
}

main()
