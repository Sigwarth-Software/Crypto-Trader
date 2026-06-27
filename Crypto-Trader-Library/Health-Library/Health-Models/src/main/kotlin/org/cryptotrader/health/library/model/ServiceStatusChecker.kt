@file:JvmName("ServiceStatusChecker")
package org.cryptotrader.health.library.model

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClientBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URI
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

private const val BASE_OVERRIDE_PROP = "ct.health.base"
private const val BASE_OVERRIDE_ENV = "CT_HEALTH_BASE"
private const val CA_BUNDLE_PROP = "ct.health.caBundle"
private const val CA_BUNDLE_ENV = "CT_HEALTH_CA_BUNDLE"
private const val DATA_CA_BUNDLE_PROP = "ct.health.caBundle.data"
private const val DATA_CA_BUNDLE_ENV = "CT_HEALTH_CA_BUNDLE_DATA"
private const val SHARED_CA_BUNDLE_ENV = "CT_CA_BUNDLE"

private val log = LoggerFactory.getLogger("org.cryptotrader.health.ServiceStatus")

private fun getBaseOverride(): String? {
    return System.getProperty(BASE_OVERRIDE_PROP)
        ?: System.getenv(BASE_OVERRIDE_ENV)
}

internal fun getUrl(service: CryptoTraderService): String {
    if (service === CryptoTraderService.DOCS) {
        return "https://sigwarth-software.github.io/Crypto-Trader/"
    }
    val host: String = if (service === CryptoTraderService.DATA) {
        System.getenv("CT_DATA_HOST") ?: "localhost"
    } else {
        "localhost"
    }
    val base = getBaseOverride()
    return if (base != null) {
        URI.create(base).resolve("/actuator/health").toString()
    } else {
        "https://${host}:${service.port}/actuator/health"
    }
}

private fun getHttpRequest(url: String): HttpGet {
    return HttpGet(url)
}

private fun getHttpClient(service: CryptoTraderService): HttpClient {
    val caBundle: String? = getCaBundle(service)
    log.info("Found CA bundle: {}", caBundle ?: "none")
    if (caBundle.isNullOrBlank()) {
        return HttpClientBuilder.create().build()
    }
    return HttpClientBuilder.create()
        .setSSLSocketFactory(getSslSocketFactory(caBundle))
        .build()
}

private fun getCaBundle(service: CryptoTraderService): String? {
    val dataCaBundle = if (service === CryptoTraderService.DATA) {
        System.getProperty(DATA_CA_BUNDLE_PROP)
            ?: System.getenv(DATA_CA_BUNDLE_ENV)
    } else {
        null
    }
    return dataCaBundle
        ?: System.getProperty(CA_BUNDLE_PROP)
        ?: System.getenv(CA_BUNDLE_ENV)
        ?: System.getenv(SHARED_CA_BUNDLE_ENV)
        ?: findLocalCaBundle()
}

private fun findLocalCaBundle(): String? {
    val workingDirectory = File(System.getProperty("user.dir")).absoluteFile
    return generateSequence(workingDirectory) { directory: File -> directory.parentFile }
        .map { directory: File -> File(directory, "certs/rootCA.pem") }
        .firstOrNull(File::isFile)
        ?.absolutePath
}

private fun getSslSocketFactory(caBundle: String): SSLConnectionSocketFactory {
    val certificateFile = File(caBundle)
    require(certificateFile.isFile) {
        "Configured health check CA bundle does not exist: ${certificateFile.absolutePath}"
    }
    val certificateFactory: CertificateFactory = CertificateFactory.getInstance("X.509")
    val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    keyStore.load(null, null)
    FileInputStream(certificateFile).use { inputStream: FileInputStream ->
        val certificates: Collection<Certificate> = certificateFactory.generateCertificates(inputStream)
        certificates.forEachIndexed { index: Int, certificate: Certificate ->
            keyStore.setCertificateEntry("ct-health-ca-$index", certificate as X509Certificate)
        }
    }
    val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(keyStore)
    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustManagerFactory.trustManagers, null)
    return SSLConnectionSocketFactory(sslContext)
}

fun isServiceAlive(service: CryptoTraderService): Boolean {
    log.info("Checking status of service: {}...", service)
    val validCode = 200
    try {
        val client: HttpClient = getHttpClient(service)
        val request: HttpGet = getHttpRequest(getUrl(service))
        val response: HttpResponse = client.execute(request) ?: return false
        val isAlive: Boolean = validCode == response.statusLine.statusCode
        log.info("Service: {} is alive: {}", service, isAlive)
        return isAlive
    } catch (exception: Exception) {
        log.error("Error checking status of service: {}", service, exception)
        return false
    }
}
