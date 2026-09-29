package org.cryptotrader.api.library.communication.response

import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset

data class PortfolioAssetResponse(
    val id: Long,
    val currencyName: String,
    val currencyCode: String,
    val shares: Double,
    val sharesValueInDollars: Double,
    val assetWalletDollars: Double,
    val totalValueInDollars: Double,
    val targetPrice: Double,
    val lastUpdated: String,
    val vendorName: String
) {
    constructor(portfolioAsset: PortfolioAsset) : this(
        portfolioAsset.id ?: 0,
        portfolioAsset.currency.name,
        portfolioAsset.currency.currencyCode,
        portfolioAsset.shares,
        portfolioAsset.sharesValueInDollars,
        portfolioAsset.assetWalletDollars,
        portfolioAsset.totalValueInDollars,
        portfolioAsset.targetPrice,
        portfolioAsset.lastUpdated.toString(),
        portfolioAsset.vendor.name
    )
}
