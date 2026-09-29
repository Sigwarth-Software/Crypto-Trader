package org.cryptotrader.api.library.communication.response

import java.time.LocalDateTime
import org.cryptotrader.api.library.entity.portfolio.PortfolioHistory

data class PortfolioHistoryResponse(
    val id: Long,
    val dollarBalance: Double,
    val shareBalance: Double,
    val totalWorth: Double,
    val valueChange: Double,
    val tradeOccurred: Boolean,
    val lastUpdated: LocalDateTime
) {
    constructor(history: PortfolioHistory) : this(
        history.id ?: 0,
        history.dollarBalance,
        history.shareBalance,
        history.totalWorth,
        history.valueChange,
        history.isTradeOccurred,
        history.lastUpdated
    )
}
