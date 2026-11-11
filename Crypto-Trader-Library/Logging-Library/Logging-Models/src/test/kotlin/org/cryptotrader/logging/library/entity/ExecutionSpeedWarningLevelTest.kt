package org.cryptotrader.logging.library.entity

import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ExecutionSpeedWarningLevel")
@UnitSuite
class ExecutionSpeedWarningLevelTest {
    @Nested
    @DisplayName("from")
    inner class From {
        @Nested
        @DisplayName("when execution takes 80 milliseconds")
        inner class WhenExecutionTakes80Milliseconds {
            @Test
            @DisplayName("should classify as exceeding")
            fun classifyAsExceeding() {
                assertEquals(ExecutionSpeedWarningLevel.EXCEEDING, classify(80))
            }
        }

        @Nested
        @DisplayName("when execution takes 81 milliseconds")
        inner class WhenExecutionTakes81Milliseconds {
            @Test
            @DisplayName("should classify as expected")
            fun classifyAsExpected() {
                assertEquals(ExecutionSpeedWarningLevel.EXPECTED, classify(81))
            }
        }

        @Nested
        @DisplayName("when execution takes 140 milliseconds")
        inner class WhenExecutionTakes140Milliseconds {
            @Test
            @DisplayName("should classify as expected")
            fun classifyAsExpected() {
                assertEquals(ExecutionSpeedWarningLevel.EXPECTED, classify(140))
            }
        }

        @Nested
        @DisplayName("when execution takes 141 milliseconds")
        inner class WhenExecutionTakes141Milliseconds {
            @Test
            @DisplayName("should classify as warning")
            fun classifyAsWarning() {
                assertEquals(ExecutionSpeedWarningLevel.WARNING, classify(141))
            }
        }

        @Nested
        @DisplayName("when execution takes 180 milliseconds")
        inner class WhenExecutionTakes180Milliseconds {
            @Test
            @DisplayName("should classify as warning")
            fun classifyAsWarning() {
                assertEquals(ExecutionSpeedWarningLevel.WARNING, classify(180))
            }
        }

        @Nested
        @DisplayName("when execution takes 181 milliseconds")
        inner class WhenExecutionTakes181Milliseconds {
            @Test
            @DisplayName("should classify as alert")
            fun classifyAsAlert() {
                assertEquals(ExecutionSpeedWarningLevel.ALERT, classify(181))
            }
        }

        @Nested
        @DisplayName("when execution takes 200 milliseconds")
        inner class WhenExecutionTakes200Milliseconds {
            @Test
            @DisplayName("should classify as alert")
            fun classifyAsAlert() {
                assertEquals(ExecutionSpeedWarningLevel.ALERT, classify(200))
            }
        }

        @Nested
        @DisplayName("when execution takes 201 milliseconds")
        inner class WhenExecutionTakes201Milliseconds {
            @Test
            @DisplayName("should classify as alert")
            fun classifyAsAlert() {
                assertEquals(ExecutionSpeedWarningLevel.ALERT, classify(201))
            }
        }
    }

    private fun classify(executionSpeed: Long) = ExecutionSpeedWarningLevel.from(
        executionSpeed,
        100,
        0.8,
        1.4,
        1.8,
        2.0,
    )
}
