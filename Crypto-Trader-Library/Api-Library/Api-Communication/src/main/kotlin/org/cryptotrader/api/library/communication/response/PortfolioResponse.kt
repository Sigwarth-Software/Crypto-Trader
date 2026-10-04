package org.cryptotrader.api.library.communication.response

import org.cryptotrader.api.library.entity.portfolio.Portfolio

data class PortfolioResponse(
    val id: Long,
    val dollarBalance: Double,
    val shareBalance: Double,
    val totalWorth: Double,
    val lastUpdated: String,
    val assets: List<PortfolioAssetResponse>
) {
    constructor(portfolio: Portfolio) : this(
        portfolio.id ?: 0,
        portfolio.dollarBalance,
        portfolio.shareBalance,
        portfolio.totalWorth,
        portfolio.lastUpdated.toString(),
        portfolio.assets.map { PortfolioAssetResponse(it) }
    )
}
