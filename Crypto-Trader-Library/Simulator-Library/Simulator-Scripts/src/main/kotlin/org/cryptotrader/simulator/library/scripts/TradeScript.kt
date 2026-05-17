package org.cryptotrader.simulator.library.scripts

import org.cryptotrader.api.library.entity.portfolio.Portfolio
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset
import org.cryptotrader.api.library.model.trade.AssetTrader
import org.cryptotrader.api.library.model.trade.TradeContext
import org.cryptotrader.data.library.entity.currency.CurrencyHistory
import java.time.LocalDateTime

fun executeAssetSimulation(
    portfolioAsset: PortfolioAsset,
    trader: AssetTrader,
    currencyHistory: CurrencyHistory
): PortfolioAsset {
    trader.trade(currencyHistory.value)
    return portfolioAsset
}

fun getSimulatedAsset(
    portfolioAsset: PortfolioAsset,
    filteredCurrencyValues: MutableList<CurrencyHistory>
): PortfolioAsset {
    val initialPrice: Double = filteredCurrencyValues.firstOrNull()?.value ?: portfolioAsset.currency.value
    portfolioAsset.targetPrice = initialPrice
    val trader = AssetTrader(portfolioAsset, TradeContext.SIMULATION)

    val isFirstPurchase: Boolean =
        portfolioAsset.shares == 0.0 && portfolioAsset.assetWalletDollars > 0.0
    if (isFirstPurchase && filteredCurrencyValues.isNotEmpty()) {
        trader.buy(initialPrice)
        portfolioAsset.targetPrice = initialPrice
    }

    filteredCurrencyValues.forEach { currencyHistory ->
        trader.trade(currencyHistory.value)
    }

    val lastHistory: CurrencyHistory? = filteredCurrencyValues.lastOrNull()
    if (lastHistory != null) {
        portfolioAsset.currency.value = lastHistory.value
        portfolioAsset.currency.lastUpdated = lastHistory.lastUpdated
    }
    portfolioAsset.updateValues()
    return portfolioAsset
}

fun getSimulatedPortfolio(
    portfolio: Portfolio,
    filteredCurrencyValues: Map<String, MutableList<CurrencyHistory>>
): Portfolio {
    val simulatedAssets: List<PortfolioAsset> =
        portfolio.assets.parallelStream()
            .map { portfolioAsset ->
                val filteredCurrencyValuesForAsset: MutableList<CurrencyHistory>? =
                    filteredCurrencyValues[portfolioAsset.currency.currencyCode]
                requireNotNull(filteredCurrencyValuesForAsset) {
                    "Currency history not found for asset: ${portfolioAsset.currency.currencyCode}"
                }
                getSimulatedAsset(
                    portfolioAsset,
                    filteredCurrencyValuesForAsset
                )
            }
            .toList()

    portfolio.assets = simulatedAssets.toMutableList()
    return portfolio
}

fun getDetailedSimulatedPortfolio(
    portfolio: Portfolio,
    filteredCurrencyValues: Map<String, MutableList<CurrencyHistory>>
): Map<String, List<Pair<LocalDateTime, Double>>> {
    return portfolio.assets.associate { portfolioAsset ->
        val detailedPortfolioAsset = PortfolioAsset.from(portfolioAsset)
        val filteredCurrencyValuesForAsset: MutableList<CurrencyHistory> =
            requireNotNull(filteredCurrencyValues[detailedPortfolioAsset.currency.currencyCode]) {
                "Currency history not found for asset: ${detailedPortfolioAsset.currency.currencyCode}"
            }

        detailedPortfolioAsset.currency.value =
            filteredCurrencyValuesForAsset.firstOrNull()?.value
                ?: detailedPortfolioAsset.currency.value
        detailedPortfolioAsset.targetPrice = detailedPortfolioAsset.currency.value

        detailedPortfolioAsset.currency.currencyCode to getDetailedSimulatedAsset(
            detailedPortfolioAsset,
            filteredCurrencyValuesForAsset
        )
    }
}

fun getDetailedSimulatedAsset(
    portfolioAsset: PortfolioAsset,
    filteredCurrencyValues: MutableList<CurrencyHistory>
): List<Pair<LocalDateTime, Double>> {
    val trader = AssetTrader(portfolioAsset, TradeContext.SIMULATION)
    val detailedValues: MutableList<Pair<LocalDateTime, Double>> = mutableListOf()

    if (portfolioAsset.shares == 0.0 && portfolioAsset.assetWalletDollars > 0.0 && filteredCurrencyValues.isNotEmpty()) {
        trader.buy(filteredCurrencyValues.first().value)
    }

    filteredCurrencyValues.forEach { currencyHistory ->
        portfolioAsset.currency.value = currencyHistory.value
        trader.trade(currencyHistory.value)
        portfolioAsset.currency.value = currencyHistory.value
        portfolioAsset.updateValues()
        detailedValues.add(currencyHistory.lastUpdated to portfolioAsset.totalValueInDollars)
    }

    return detailedValues
}

fun handleInitialPurchase(portfolioAsset: PortfolioAsset, filteredCurrencyValues: MutableList<CurrencyHistory>) {
    val initialPrice: Double = filteredCurrencyValues.firstOrNull()?.value ?: portfolioAsset.currency.value
    portfolioAsset.targetPrice = initialPrice
    val trader = AssetTrader(portfolioAsset, TradeContext.SIMULATION)

    val isFirstPurchase: Boolean =
        portfolioAsset.shares == 0.0 && portfolioAsset.assetWalletDollars > 0.0
    if (isFirstPurchase && filteredCurrencyValues.isNotEmpty()) {
        trader.buy(initialPrice)
        portfolioAsset.targetPrice = initialPrice
    }
}


fun getDetailedNaturalPortfolio(portfolio: Portfolio, filteredCurrencyValues: Map<String, MutableList<CurrencyHistory>>): Map<String, List<Pair<LocalDateTime, Double>>> {
    return portfolio.assets.associate { portfolioAsset ->
        val detailedPortfolioAsset = PortfolioAsset.from(portfolioAsset)
        val filteredCurrencyValuesForAsset: MutableList<CurrencyHistory> =
            requireNotNull(filteredCurrencyValues[detailedPortfolioAsset.currency.currencyCode]) {
                "Currency history not found for asset: ${detailedPortfolioAsset.currency.currencyCode}"
            }

        detailedPortfolioAsset.currency.currencyCode to getDetailedNaturalAsset(
            detailedPortfolioAsset,
            filteredCurrencyValuesForAsset
        )
    }
}

fun getDetailedNaturalAsset(portfolioAsset: PortfolioAsset, filteredCurrencyValues: MutableList<CurrencyHistory>): List<Pair<LocalDateTime, Double>> {
    val numShares: Double = portfolioAsset.shares
    val numDollars: Double = portfolioAsset.assetWalletDollars
    val initialPrice: Double = filteredCurrencyValues.firstOrNull()?.value ?: portfolioAsset.currency.value

    if (initialPrice <= 0.0) {
        return filteredCurrencyValues.map { it.lastUpdated to (it.value * numShares) + numDollars }
    }

    val sharesFromDollars: Double = numDollars / initialPrice
    val totalShares: Double = numShares + sharesFromDollars

    return filteredCurrencyValues.map { currencyHistory ->
        currencyHistory.lastUpdated to totalShares * currencyHistory.value
    }
}
