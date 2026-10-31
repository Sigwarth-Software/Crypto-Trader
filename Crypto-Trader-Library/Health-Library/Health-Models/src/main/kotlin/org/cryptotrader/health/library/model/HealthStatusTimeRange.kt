package org.cryptotrader.health.library.model

enum class HealthStatusTimeRange(val timeRange: String) {
    NOW("now"),
    DAY("day"),
    WEEK("week"),
    MONTH("month")
}