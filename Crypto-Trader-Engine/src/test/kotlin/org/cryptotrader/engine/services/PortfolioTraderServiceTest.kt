package org.cryptotrader.engine.services

import jakarta.persistence.EntityManager
import java.lang.reflect.Method
import java.time.LocalDateTime
import org.cryptotrader.api.library.entity.portfolio.Portfolio
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset
import org.cryptotrader.api.library.entity.portfolio.PortfolioAssetHistory
import org.cryptotrader.api.library.entity.portfolio.PortfolioHistory
import org.cryptotrader.api.library.model.trade.Trader
import org.cryptotrader.api.library.services.PortfolioService
import org.cryptotrader.api.library.services.TradeEventService
import org.cryptotrader.data.library.entity.currency.Currency
import org.cryptotrader.engine.library.services.PortfolioTradeExecutionService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

@DisplayName("PortfolioTradeExecutionService")
@UnitSuite
class PortfolioTraderServiceTest : CryptoTraderTest() {

    private lateinit var portfolioService: PortfolioService
    private lateinit var tradeEventService: TradeEventService
    private lateinit var entityManager: EntityManager
    private lateinit var service: PortfolioTradeExecutionService

    @BeforeEach
    fun setUp() {
        this.portfolioService = mock(PortfolioService::class.java)
        this.tradeEventService = mock(TradeEventService::class.java)
        this.entityManager = mock(EntityManager::class.java)
        this.service = PortfolioTradeExecutionService(
            this.portfolioService,
            this.tradeEventService,
            this.entityManager
        )
    }

    @Nested
    @DisplayName("saveAssetChanges")
    inner class AssetChangePersistence {
        @Nested
        @DisplayName("when previous history has shares")
        inner class WhenPreviousHistoryHasShares {
            private lateinit var portfolio: Portfolio
            private lateinit var asset: PortfolioAsset
            private lateinit var trader: Trader
            private lateinit var previous: PortfolioAssetHistory

            @BeforeEach
            fun setUp() {
                val currency = Currency().apply {
                    currencyCode = "USD"; name = "US Dollar"; value = 1.0
                }
                portfolio = Portfolio().apply { this.id = 77L }
                asset = PortfolioAsset(portfolio, currency, 2.5, 0.0).apply { this.id = 88L }
                trader = Trader(portfolio)
                previous = PortfolioAssetHistory(asset, LocalDateTime.now().minusMinutes(5)).apply {
                    shares = 1.5
                    totalValueInDollars = 150.0
                }
                `when`(portfolioService.getLatestPreviousAssetHistoryWithShares(
                    any(PortfolioAssetHistory::class.java)
                )).thenReturn(previous)
                `when`(portfolioService.getLatestPortfolioAssetHistory(asset)).thenReturn(previous)
                `when`(portfolioService.getLatestPortfolioHistory(portfolio)).thenReturn(null as PortfolioHistory?)
            }

            @Test
            @DisplayName("should calculate the delta and persist histories")
            fun calculateDeltaAndPersistHistories() {
                invokeSaveAssetChanges(trader, asset, true)

                val prevSharesCaptor = ArgumentCaptor.forClass(PortfolioAssetHistory::class.java)
                val currSharesCaptor = ArgumentCaptor.forClass(PortfolioAssetHistory::class.java)
                verify(portfolioService).setPortfolioSharesChange(prevSharesCaptor.capture(), currSharesCaptor.capture())

                val usedPrevForShares = prevSharesCaptor.value
                val currentForShares = currSharesCaptor.value
                assertNotNull(currentForShares)
                assertEquals(previous.shares, usedPrevForShares.shares, 1e-9)

                verify(portfolioService).savePortfolioAsset(asset)
                verify(portfolioService).savePortfolio(portfolio)
                verify(portfolioService).savePortfolioAssetHistory(any(PortfolioAssetHistory::class.java))
                verify(portfolioService).savePortfolioHistory(any(PortfolioHistory::class.java))
            }
        }

        @Nested
        @DisplayName("when zero shares occur between nonzero histories")
        inner class WhenZeroSharesOccurBetweenNonzeroHistories {
            private lateinit var asset: PortfolioAsset
            private lateinit var trader: Trader

            @BeforeEach
            fun setUp() {
                val currency = Currency().apply { currencyCode = "USD"; name = "US Dollar"; value = 1.0 }
                val portfolio = Portfolio().apply { this.id = 101L }
                asset = PortfolioAsset(portfolio, currency, 10.0, 0.0).apply { this.id = 202L }
                trader = Trader(portfolio)
                val sixPm = LocalDateTime.now().minusHours(2)
                val sevenPm = LocalDateTime.now().minusHours(1)
                val nonZeroAtSix = PortfolioAssetHistory(asset, sixPm).apply { shares = 5.0 }
                val zeroAtSeven = PortfolioAssetHistory(asset, sevenPm).apply { shares = 0.0 }
                `when`(portfolioService.getLatestPortfolioAssetHistory(asset)).thenReturn(zeroAtSeven)
                `when`(portfolioService.getLatestPreviousAssetHistoryWithShares(
                    any(PortfolioAssetHistory::class.java)
                ))
                    .thenReturn(nonZeroAtSix)
                `when`(portfolioService.getLatestPortfolioHistory(portfolio)).thenReturn(null as PortfolioHistory?)
            }

            @Test
            @DisplayName("should calculate the change from the last nonzero shares")
            fun useLastNonzeroShares() {
                // Act
                invokeSaveAssetChanges(trader, asset, true)

                val prevSharesCaptor = ArgumentCaptor.forClass(PortfolioAssetHistory::class.java)
                val currSharesCaptor = ArgumentCaptor.forClass(PortfolioAssetHistory::class.java)
                verify(portfolioService).setPortfolioSharesChange(prevSharesCaptor.capture(), currSharesCaptor.capture())
                val usedPrev = prevSharesCaptor.value
                assertNotNull(usedPrev)
                assertEquals(5.0, usedPrev.shares, 1e-9) // baseline is 6pm with 5 shares
                // And history is persisted
                verify(portfolioService).savePortfolioAssetHistory(any(PortfolioAssetHistory::class.java))
            }
        }

        @Nested
        @DisplayName("when shares decrease")
        inner class WhenSharesDecrease {
            private lateinit var asset: PortfolioAsset
            private lateinit var trader: Trader

            @BeforeEach
            fun setUp() {
                val currency = Currency().apply { currencyCode = "USD"; name = "US Dollar"; value = 1.0 }
                val portfolio = Portfolio().apply { this.id = 303L }
                asset = PortfolioAsset(portfolio, currency, 4.0, 0.0).apply { this.id = 404L }
                trader = Trader(portfolio)
                val previous = PortfolioAssetHistory(asset, LocalDateTime.now().minusMinutes(10)).apply { shares = 10.0 }
                `when`(portfolioService.getLatestPortfolioAssetHistory(asset)).thenReturn(previous)
                `when`(portfolioService.getLatestPreviousAssetHistoryWithShares(any(PortfolioAssetHistory::class.java)))
                    .thenReturn(previous)
                `when`(portfolioService.getLatestPortfolioHistory(portfolio)).thenReturn(null as PortfolioHistory?)
            }

            @Test
            @DisplayName("should not invoke share change")
            fun notInvokeShareChange() {
                invokeSaveAssetChanges(trader, asset, true)

                val savedHistoryCaptor = ArgumentCaptor.forClass(PortfolioAssetHistory::class.java)
                verify(portfolioService).savePortfolioAssetHistory(savedHistoryCaptor.capture())
                val saved = savedHistoryCaptor.value
                assertNotNull(saved)
                assertEquals(0.0, saved.sharesChange, 1e-9) // Decrease from 10 to 4 → 0 (sells do not count)
            }
        }
    }

    private fun invokeSaveAssetChanges(trader: Trader, asset: PortfolioAsset, tradeOccurred: Boolean) {
        val method: Method = PortfolioTradeExecutionService::class.java.getDeclaredMethod(
            "saveAssetChanges",
            Trader::class.java,
            PortfolioAsset::class.java,
            Boolean::class.javaPrimitiveType
        )
        method.isAccessible = true
        method.invoke(service, trader, asset, tradeOccurred)
    }
}
