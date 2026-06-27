#!/usr/bin/env kotlin

import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import kotlin.system.exitProcess

fun executableName(base: String): String {
    val osName = System.getProperty("os.name").lowercase()
    return if (osName.contains("windows")) "$base.exe" else base
}

fun findRepoRoot(): File =
    generateSequence(File(".").absoluteFile.normalize()) { directory: File -> directory.parentFile }
        .firstOrNull { directory: File ->
            File(directory, "pom.xml").isFile && File(directory, "Crypto-Trader-Data").isDirectory
        }
        ?: error("Run this script from the Crypto-Trader repository or one of its subdirectories.")

fun hasMkcert(): Boolean =
    try {
        val process = ProcessBuilder(executableName("mkcert"), "-version")
            .redirectErrorStream(true)
            .start()
        process.waitFor() == 0
    } catch (_: Exception) {
        false
    }

fun runCommand(workingDirectory: File, vararg command: String) {
    val process = ProcessBuilder(*command)
        .directory(workingDirectory)
        .inheritIO()
        .start()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        System.err.println("Command failed with exit code $exitCode: ${command.joinToString(" ")}")
        exitProcess(exitCode)
    }
}

fun captureCommand(workingDirectory: File, vararg command: String): String {
    val process = ProcessBuilder(*command)
        .directory(workingDirectory)
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

fun requireLocalIpv4(value: String): String {
    val address = try {
        InetAddress.getByName(value)
    } catch (_: Exception) {
        throw IllegalArgumentException("Invalid Data IPv4 address: $value")
    }
    require(address is Inet4Address && address.hostAddress == value && address.isSiteLocalAddress) {
        "Expected a private LAN IPv4 address such as 192.168.1.123, but received: $value"
    }
    return value
}

fun main() {
    if (!hasMkcert()) {
        System.err.println("mkcert is not installed or is not on PATH.")
        exitProcess(1)
    }

    val dataIp = requireLocalIpv4(
        args.firstOrNull()
            ?: System.getenv("CT_DATA_HOST")
            ?: error("Pass the Data computer's LAN IP, for example: kotlin scripts/setup-data-https.kts 192.168.1.123")
    )
    val repoRoot = findRepoRoot()
    val certDir = File(repoRoot, "certs").apply { mkdirs() }
    val certFile = File(certDir, "crypto-trader-data.pem")
    val keyFile = File(certDir, "crypto-trader-data-key.pem")
    val exportedRootCaFile = File(certDir, "crypto-trader-data-rootCA.pem")

    println("Installing this computer's mkcert CA into its local trust store...")
    runCommand(repoRoot, executableName("mkcert"), "-install")

    println("Generating the Data certificate for LAN IP $dataIp...")
    runCommand(
        repoRoot,
        executableName("mkcert"),
        "-key-file", keyFile.absolutePath,
        "-cert-file", certFile.absolutePath,
        dataIp,
        "crypto-trader-data",
        "localhost",
        "127.0.0.1",
        "::1",
    )

    val mkcertCaRoot = captureCommand(repoRoot, executableName("mkcert"), "-CAROOT")
    val mkcertRootCaFile = File(mkcertCaRoot, "rootCA.pem")
    require(mkcertRootCaFile.isFile) {
        "mkcert root certificate was not found at ${mkcertRootCaFile.absolutePath}"
    }
    mkcertRootCaFile.copyTo(exportedRootCaFile, overwrite = true)

    println(
        """

        Data HTTPS files are ready:
          Certificate: ${certFile.absolutePath}
          Private key: ${keyFile.absolutePath}
          Public CA certificate for Engine: ${exportedRootCaFile.absolutePath}

        Start Crypto-Trader-Data on this computer:
          ${'$'}env:SERVER_SSL_ENABLED='true'
          ${'$'}env:SERVER_SSL_CERTIFICATE_DATA='${certFile.toURI().toASCIIString()}'
          ${'$'}env:SERVER_SSL_CERTIFICATE_PRIVATE_KEY_DATA='${keyFile.toURI().toASCIIString()}'
          mvn -pl Crypto-Trader-Data -am spring-boot:run

        Copy only crypto-trader-data-rootCA.pem to the Engine computer, then configure Engine:
          ${'$'}env:CT_DATA_HOST='$dataIp'
          ${'$'}env:CT_HEALTH_CA_BUNDLE_DATA='C:\path\to\crypto-trader-data-rootCA.pem'
          mvn -pl Crypto-Trader-Engine -am spring-boot:run

        Never copy mkcert's rootCA-key.pem.
        """.trimIndent()
    )
}

main()
