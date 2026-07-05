package org.cryptotrader.api.library.model.trade

enum class TradeResult(val resultLabel: String) {
    BUY("BUY"),
    SELL("SELL"),
    HOLD("HOLD"),
    FAIL("FAIL");

    companion object {
        fun fromLabel(label: String): TradeResult {
            return entries.find { it.resultLabel.equals(label, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown TradeResult label: $label")
        }
    }
}