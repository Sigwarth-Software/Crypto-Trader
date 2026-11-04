package org.cryptotrader.health.library.model

enum class HealthStatusTimeRange(val timeRange: String) {
    NOW("now"),
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    QUARTER("quarter");

    companion object {
        fun from(timeRange: String): HealthStatusTimeRange {
            return entries.firstOrNull {
                it.timeRange.equals(timeRange, ignoreCase = true)
            } ?: throw IllegalArgumentException("Invalid time range: $timeRange")
        }
    }

    fun getNumRecords(): Int {
        return when (this) {
            NOW -> 1
            DAY -> 24
            WEEK -> 7
            MONTH -> 30
            QUARTER -> 90
        }
    }
}