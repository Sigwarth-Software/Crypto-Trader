package org.cryptotrader.api.library.entity.user

enum class SubscriptionTier(val label: String, val level: Int, val intervalMs: Long) {
    FREE("Free", 1, 30000L),
    PRO("Pro", 2,  1000L),
    ULTIMATE("Ultimate", 3, 5000L);

    companion object {
        fun fromLabel(label: String): SubscriptionTier? {
            return entries.find { it.label == label }
        }
    }
}
