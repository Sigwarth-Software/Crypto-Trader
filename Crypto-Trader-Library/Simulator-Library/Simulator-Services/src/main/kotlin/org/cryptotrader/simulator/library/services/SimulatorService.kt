package org.cryptotrader.simulator.library.services

import org.cryptotrader.api.library.communication.request.FuzzyTimeValueRequest
import org.cryptotrader.api.library.entity.portfolio.Portfolio
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset
import org.cryptotrader.api.library.entity.user.SubscriptionTier
import org.cryptotrader.data.library.entity.currency.Currency
import org.cryptotrader.data.library.entity.currency.CurrencyHistory
import org.cryptotrader.data.library.repository.CurrencyHistoryRepository
import org.cryptotrader.data.library.services.CurrencyService
import org.cryptotrader.simulator.library.communication.request.DetailedPortfolioSimulationRequest
import org.cryptotrader.simulator.library.communication.request.PortfolioSimulationRequest
import org.cryptotrader.simulator.library.communication.response.DetailedPortfolioSimulationResponse
import org.cryptotrader.simulator.library.communication.response.PortfolioSimulationResponse
import org.cryptotrader.simulator.library.model.DownsampleDuration
import org.cryptotrader.universal.library.communication.response.CompareTimeValueResponse
import org.cryptotrader.universal.library.communication.response.TimeValueResponse
import org.cryptotrader.simulator.library.scripts.getDetailedNaturalPortfolio
import org.cryptotrader.simulator.library.scripts.getDetailedSimulatedPortfolio
import org.cryptotrader.simulator.library.scripts.getSimulatedAsset
import org.cryptotrader.simulator.library.scripts.getSimulatedPortfolio
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Service
class SimulatorService @Autowired constructor(
    private val currencyHistoryRepository: CurrencyHistoryRepository,
    private val currencyService: CurrencyService
) {
    private val log: Logger = LoggerFactory.getLogger(SimulatorService::class.java)

    fun simulate(
        portfolioSimulationRequest: PortfolioSimulationRequest,
        cachedPortfolioCurrencies: Map<String, MutableList<CurrencyHistory>>? = null
    ): PortfolioSimulationResponse {
        val startDate: LocalDateTime = portfolioSimulationRequest.startDate
        val endDate: LocalDateTime = portfolioSimulationRequest.endDate
        val portfolio: Portfolio =
            this.getPortfolioFromRequest(portfolioSimulationRequest, startDate)

        val originalInvestments: Map<String, Pair<Double, Double>> = portfolio.assets.associate { asset ->
            asset.currency.currencyCode to Pair(asset.shares, asset.assetWalletDollars)
        }

        val portfolioCurrencies: Map<String, MutableList<CurrencyHistory>> = cachedPortfolioCurrencies ?: this.getCurrencyHistoryMapByPortfolio(portfolio, startDate, endDate)

        val updatedPortfolio: Portfolio = getSimulatedPortfolio(portfolio, portfolioCurrencies)
        require(portfolio.assets.size == updatedPortfolio.assets.size) {
            "Simulated portfolio assets size does not match original portfolio assets size"
        }

        updatedPortfolio.updateValues()
        val cryptoTraderValue: Double = updatedPortfolio.totalWorth

        var naturalValue = 0.0
        originalInvestments.forEach { (currencyCode, investment) ->
            val (originalShares, originalDollars) = investment
            val currencyList = portfolioCurrencies[currencyCode]
                ?: throw IllegalStateException("Currency list is null for currency $currencyCode")
            val lastCurrency: CurrencyHistory = currencyList.lastOrNull() ?: run {
                naturalValue += originalDollars
                return@forEach
            }
            val firstCurrency = currencyList.firstOrNull()
            if (firstCurrency != null && firstCurrency.value > 0) {
                val sharesFromDollars = originalDollars / firstCurrency.value
                naturalValue += (originalShares + sharesFromDollars) * lastCurrency.value
            } else {
                naturalValue += lastCurrency.value * originalShares
                naturalValue += originalDollars
            }
        }

        return PortfolioSimulationResponse(
            cryptoTraderValue,
            naturalValue,
        )
    }

    private fun getPortfolioAssetsFromRequest(
        portfolioSimulationRequest: PortfolioSimulationRequest,
        startDate: LocalDateTime,
        portfolio: Portfolio
    ): List<PortfolioAsset> {
        val portfolioAssets: List<PortfolioAsset> =
            portfolioSimulationRequest.assetSimulationRequests.map {
                val currency: Currency =
                    this.currencyService.getCurrencyByCurrencyCode(it.currencyCode)
                val fuzzyTimeValue: TimeValueResponse =
                    this.currencyService.getFuzzyCurrencyHistory(
                        it.currencyCode,
                        FuzzyTimeValueRequest(
                            startDate.toString(),
                        )
                    )
                currency.value = fuzzyTimeValue.value
                currency.lastUpdated =
                    LocalDateTime.parse(fuzzyTimeValue.timestamp)
                val currencyAtDate = CurrencyHistory(
                    currency,
                    fuzzyTimeValue.value,
                    LocalDateTime.parse(fuzzyTimeValue.timestamp)
                )
                val portfolioAsset: PortfolioAsset = PortfolioAsset.builder()
                    .portfolio(portfolio)
                    .currency(Currency.fromHistory(currencyAtDate))
                    .shares(it.numShares)
                    .assetWalletDollars(it.numDollars)
                    .build()
                // Initialize targetPrice from currency value (Lombok @Builder bypasses constructors)
                portfolioAsset.targetPrice = currencyAtDate.value
                portfolioAsset.updateValues()
                portfolioAsset
            }
        return portfolioAssets
    }


    fun detailedSimulate(detailedSimulationRequest: DetailedPortfolioSimulationRequest): DetailedPortfolioSimulationResponse {
        val startDate: LocalDateTime = detailedSimulationRequest.startDate
        val endDate: LocalDateTime = detailedSimulationRequest.endDate
        val portfolio: Portfolio = this.getPortfolioFromRequest(detailedSimulationRequest, startDate)
        val currencyHistoryMap: Map<String, MutableList<CurrencyHistory>> = this.getCurrencyHistoryMapByPortfolio(portfolio, startDate, endDate)
        val topLevelSimulation: PortfolioSimulationResponse = this.simulate(detailedSimulationRequest, currencyHistoryMap)
        val simulatedValues: Map<String, List<Pair<LocalDateTime, Double>>> = getDetailedSimulatedPortfolio(portfolio, currencyHistoryMap)
        val naturalValues: Map<String, List<Pair<LocalDateTime, Double>>> = getDetailedNaturalPortfolio(portfolio, currencyHistoryMap)
        val sharedSize: Int = minOf(
            simulatedValues.values.minOfOrNull { it.size } ?: 0,
            naturalValues.values.minOfOrNull { it.size } ?: 0
        )

        val points: MutableList<CompareTimeValueResponse> = mutableListOf()

        simulatedValues.values.firstOrNull()?.take(sharedSize)?.forEachIndexed { index, (timestamp, _) ->
            val cryptoTraderValueAtTimestamp: Double = simulatedValues.values.sumOf { it[index].second }
            val naturalValueAtTimestamp: Double = naturalValues.values.sumOf { it[index].second }
            points += CompareTimeValueResponse(
                timestamp.toString(),
                cryptoTraderValueAtTimestamp,
                naturalValueAtTimestamp
            )
        }
        val downsampledPoints = downsamplePoints(points, detailedSimulationRequest.downsampleDuration)
        return DetailedPortfolioSimulationResponse(
            downsampledPoints,
            topLevelSimulation.cryptoTraderProfit,
            topLevelSimulation.naturalProfit,
        )
    }

    private fun downsamplePoints(
        points: List<CompareTimeValueResponse>,
        downsampleDuration: DownsampleDuration = DownsampleDuration.HOUR
    ): List<CompareTimeValueResponse> {
        if (points.isEmpty()) {
            return points
        }

        fun bucketKey(timestamp: LocalDateTime): Any = when (downsampleDuration) {
            DownsampleDuration.MINUTE -> timestamp.truncatedTo(ChronoUnit.MINUTES)
            DownsampleDuration.HOUR -> timestamp.truncatedTo(ChronoUnit.HOURS)
            DownsampleDuration.DAY -> timestamp.toLocalDate()
            DownsampleDuration.MONTH -> YearMonth.from(timestamp)
        }

        return points
            .groupBy { bucketKey(LocalDateTime.parse(it.timestamp)) }
            .values
            .map { bucket -> bucket.last() }
            .sortedBy { LocalDateTime.parse(it.timestamp) }
    }

    private fun getPortfolioFromRequest(
        detailedSimulationRequest: PortfolioSimulationRequest,
        startDate: LocalDateTime
    ): Portfolio {
        val portfolio = Portfolio()
        val portfolioAssets: List<PortfolioAsset> =
            this.getPortfolioAssetsFromRequest(
                detailedSimulationRequest,
                startDate,
                portfolio
            )
        portfolio.assets = portfolioAssets
        return portfolio
    }

    fun simulatePortfolio(portfolio: Portfolio, startDate: LocalDateTime, endDate: LocalDateTime): Portfolio {
        return getSimulatedPortfolio(
            portfolio,
            this.getCurrencyHistoryMapByPortfolio(portfolio, startDate, endDate)
        )
    }

    fun getCurrencyHistoryMapByPortfolio(portfolio: Portfolio, startDate: LocalDateTime, endDate: LocalDateTime): Map<String, MutableList<CurrencyHistory>> {
        val currencyHistoryMap: MutableMap<String, MutableList<CurrencyHistory>> = mutableMapOf()
        portfolio.assets.forEach { portfolioAsset ->
            val currencyHistory: MutableList<CurrencyHistory> = this.getCurrencyHistoryByPortfolio(
                portfolio,
                portfolioAsset.currency,
                startDate,
                endDate
            )
            currencyHistoryMap[portfolioAsset.currency.currencyCode] = currencyHistory
        }
        return currencyHistoryMap
    }

    fun getSimulatedPortfolioAsset(portfolioAsset: PortfolioAsset, startDate: LocalDateTime, endDate: LocalDateTime): PortfolioAsset {
        return getSimulatedAsset(portfolioAsset,
            this.getCurrencyHistoryByPortfolio(
                portfolioAsset.portfolio,
                portfolioAsset.currency,
                startDate,
                endDate
            ))
    }

    fun getCurrencyHistoryByPortfolio(
        portfolio: Portfolio,
        currency: Currency,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): MutableList<CurrencyHistory> {
        val subscriptionTier: SubscriptionTier = portfolio.user.subscriptionTier
        val currencyCode: String = currency.currencyCode
        val intervalMs: Long = subscriptionTier.intervalMs

        val results = this.currencyHistoryRepository.findHistoryByInterval(
            currencyCode, startDate, endDate, intervalMs
        )

        val resultSet: MutableList<CurrencyHistory> = mutableListOf()

        results.forEach { row ->
            val cell = row[0]
            val timestamp: LocalDateTime? = when (cell) {
                is Timestamp -> cell.toLocalDateTime()
                is LocalDateTime -> cell
                is Instant -> LocalDateTime.ofInstant(cell, ZoneId.systemDefault())
                is OffsetDateTime -> cell.toLocalDateTime()
                is ZonedDateTime -> cell.toLocalDateTime()
                else -> {
                    if (cell != null) {
                        log.warn("Unknown timestamp type: ${cell.javaClass.name}")
                    }
                    null
                }
            }
            val value: Double? = (row[1] as Number?)?.toDouble()
            if (timestamp != null && value != null) {
                val history = CurrencyHistory()
                history.currency = currency
                history.lastUpdated = timestamp
                history.value = value
                resultSet.add(history)
            }
        }
        log.info("Found {} currency history records for {} from {} to {}", resultSet.size, currencyCode, startDate, endDate)
        return resultSet
    }
}
