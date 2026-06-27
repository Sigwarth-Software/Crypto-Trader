package org.cryptotrader.logging.library.entity

enum class ExecutionSpeedWarningLevel {
    EXCEEDING,
    EXPECTED,
    WARNING,
    ALERT;

    companion object {
        @JvmStatic
        fun from(
            executionSpeed: Long,
            expectedExecutionSpeed: Long?,
            exceedingFactor: Double,
            expectedFactor: Double,
            warningFactor: Double,
            alertFactor: Double,
        ): ExecutionSpeedWarningLevel {
            val expectedMillis = maxOf(1L, expectedExecutionSpeed ?: -1L)

            return when {
                executionSpeed <= expectedMillis * exceedingFactor -> EXCEEDING
                executionSpeed <= expectedMillis * expectedFactor -> EXPECTED
                executionSpeed <= expectedMillis * warningFactor -> WARNING
                executionSpeed <= expectedMillis * alertFactor -> ALERT
                else -> ALERT
            }
        }
    }
}
