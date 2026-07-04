package org.cryptotrader.logging.library.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExecutionSpeedWarningLevelTest {
    @Test
    fun `classifies execution speed using configured factors`() {
        assertEquals(ExecutionSpeedWarningLevel.EXCEEDING, classify(80))
        assertEquals(ExecutionSpeedWarningLevel.EXPECTED, classify(81))
        assertEquals(ExecutionSpeedWarningLevel.EXPECTED, classify(140))
        assertEquals(ExecutionSpeedWarningLevel.WARNING, classify(141))
        assertEquals(ExecutionSpeedWarningLevel.WARNING, classify(180))
        assertEquals(ExecutionSpeedWarningLevel.ALERT, classify(181))
        assertEquals(ExecutionSpeedWarningLevel.ALERT, classify(200))
        assertEquals(ExecutionSpeedWarningLevel.ALERT, classify(201))
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
