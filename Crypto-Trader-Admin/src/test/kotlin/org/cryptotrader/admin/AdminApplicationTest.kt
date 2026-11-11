package org.cryptotrader.admin

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javafx.application.Platform
import javafx.stage.Stage
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.IntegrationSuite
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@DisplayName("AdminApplication")
@IntegrationSuite
class AdminApplicationTest : CryptoTraderTest() {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {
            val latch = CountDownLatch(1)
            try {
                Platform.startup { latch.countDown() }
            } catch (ex: IllegalStateException) {
                latch.countDown()
            }
            latch.await(10, TimeUnit.SECONDS)
        }
    }

    private fun runOnFxThreadAndWait(action: () -> Unit) {
        if (Platform.isFxApplicationThread()) {
            action()
            return
        }
        val latch = CountDownLatch(1)
        val error = AtomicReference<Throwable?>()
        Platform.runLater {
            try {
                action()
            } catch (throwable: Throwable) {
                error.set(throwable)
            } finally {
                latch.countDown()
            }
        }
        latch.await(10, TimeUnit.SECONDS)
        error.get()?.let { throw it }
    }

    @Nested
    @DisplayName("start")
    inner class Start {
        @Test
        @DisplayName("should start without exceptions")
        fun startWithoutExceptions() {
            assertDoesNotThrow {
                val app = AdminApplication()
                app.init()
                val stageRef = AtomicReference<Stage>()
                runOnFxThreadAndWait {
                    val stage = Stage()
                    stageRef.set(stage)
                    app.start(stage)
                }
                runOnFxThreadAndWait {
                    stageRef.get()?.close()
                    app.stop()
                }
            }
        }
    }
}
