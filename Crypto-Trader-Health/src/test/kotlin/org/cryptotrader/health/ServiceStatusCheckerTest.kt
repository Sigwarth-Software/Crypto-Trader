package org.cryptotrader.health

import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.cryptotrader.health.library.model.CryptoTraderService
import org.cryptotrader.health.library.model.isServiceAlive
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@DisplayName("Service Status Checker")
class ServiceStatusCheckerTest : CryptoTraderTest() {

    private lateinit var server: HttpServer
    private lateinit var healthContext: HttpContext
    private lateinit var statusHandler: MutableStatusHandler
    private var port: Int = 8000
    private var sandboxPort: Int = -1


    @BeforeEach
    fun setUp() {
        this.server = HttpServer.create(InetSocketAddress(0), 0)
        this.sandboxPort = this.server.address.port


        this.statusHandler = MutableStatusHandler(initialStatusCode = 200)
        this.healthContext = this.server.createContext("/actuator/health", this.statusHandler)
        this.server.executor = Executors.newSingleThreadExecutor()
        this.server.start()

        System.setProperty("ct.health.base", "http://localhost:$sandboxPort")
    }


    @Nested
    @DisplayName("Is Service Alive")
    inner class IsServiceAlive {
        @Test
        @DisplayName("Should report status health")
        fun isServiceAlive_shouldReportStatusHealth() {
            val service = CryptoTraderService.ANALYSIS
            var expectedAliveStatus = true
            var actualAliveStatus: Boolean = isServiceAlive(service)
            assertEquals(expectedAliveStatus, actualAliveStatus)

            statusHandler.updateStatus(503)

            expectedAliveStatus = false
            actualAliveStatus = isServiceAlive(CryptoTraderService.API)
            assertEquals(expectedAliveStatus, actualAliveStatus)
        }
    }

    private class MutableStatusHandler(initialStatusCode: Int) : HttpHandler {
        @Volatile
        private var statusCode: Int = initialStatusCode

        fun updateStatus(newStatusCode: Int) {
            this.statusCode = newStatusCode
        }

        override fun handle(exchange: HttpExchange) {
            val isUp = statusCode == 200
            val body = """{"status":"${if (isUp) "UP" else "DOWN"}"}"""
            val bytes = body.toByteArray(StandardCharsets.UTF_8)
            exchange.sendResponseHeaders(statusCode, bytes.size.toLong())
            exchange.responseBody.use { out -> out.write(bytes) }
        }
    }

    @AfterEach
    fun tearDown() {
        System.clearProperty("ct.health.base")
        server.stop(0)
        (server.executor as? ExecutorService)?.shutdownNow()
    }
}
