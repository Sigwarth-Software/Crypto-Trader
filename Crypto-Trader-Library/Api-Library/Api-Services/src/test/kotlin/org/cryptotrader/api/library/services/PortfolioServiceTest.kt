package org.cryptotrader.api.library.services

import java.time.LocalDateTime
import org.cryptotrader.api.library.entity.portfolio.Portfolio
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset
import org.cryptotrader.api.library.entity.portfolio.PortfolioAssetHistory
import org.cryptotrader.api.library.repository.PortfolioAssetHistoryRepository
import org.cryptotrader.api.library.repository.PortfolioAssetRepository
import org.cryptotrader.api.library.repository.PortfolioHistoryRepository
import org.cryptotrader.api.library.repository.PortfolioRepository
import org.cryptotrader.api.library.services.entity.portfolio.PortfolioAssetEntityService
import org.cryptotrader.api.library.services.entity.portfolio.PortfolioAssetHistoryEntityService
import org.cryptotrader.api.library.services.entity.portfolio.PortfolioEntityService
import org.cryptotrader.api.library.services.entity.portfolio.PortfolioHistoryEntityService
import org.cryptotrader.data.library.entity.currency.Currency
import org.cryptotrader.data.library.services.CurrencyService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@DisplayName("PortfolioService")
@UnitSuite
class PortfolioServiceTest : CryptoTraderTest() {

    private lateinit var portfolioRepository: PortfolioRepository
    private lateinit var portfolioAssetRepository: PortfolioAssetRepository
    private lateinit var portfolioHistoryRepository: PortfolioHistoryRepository
    private lateinit var portfolioAssetHistoryRepository: PortfolioAssetHistoryRepository
    private lateinit var currencyService: CurrencyService
    private lateinit var portfolioEntityService: PortfolioEntityService
    private lateinit var portfolioAssetEntityService: PortfolioAssetEntityService
    private lateinit var portfolioHistoryEntityService: PortfolioHistoryEntityService
    private lateinit var portfolioAssetHistoryEntityService: PortfolioAssetHistoryEntityService

    private lateinit var portfolioService: PortfolioService

    @BeforeEach
    fun setUp() {
        this.portfolioRepository = mock()
        this.portfolioAssetRepository = mock()
        this.portfolioHistoryRepository = mock()
        this.portfolioAssetHistoryRepository = mock()
        this.currencyService = mock()
        this.portfolioEntityService = mock()
        this.portfolioAssetEntityService = mock()
        this.portfolioHistoryEntityService = mock()
        this.portfolioAssetHistoryEntityService = mock()

        this.portfolioService = PortfolioService(
            this.portfolioRepository,
            this.portfolioAssetRepository,
            this.portfolioHistoryRepository,
            this.portfolioAssetHistoryRepository,
            this.currencyService,
            this.portfolioEntityService,
            this.portfolioAssetEntityService,
            this.portfolioHistoryEntityService,
            this.portfolioAssetHistoryEntityService
        )
    }

    @Nested
    @DisplayName("getLatestPreviousAssetHistoryWithShares")
    inner class GetLatestPreviousAssetHistoryWithShares {

        @Nested
        @DisplayName("when current history is null")
        inner class WhenCurrentHistoryIsNull {
            @Test
            @DisplayName("should return null")
            fun returnNull() {
                val result = portfolioService.getLatestPreviousAssetHistoryWithShares(null)
                assertNull(result)
            }
        }

        @Nested
        @DisplayName("when current history lacks asset")
        inner class WhenCurrentHistoryLacksAsset {
            private lateinit var current: PortfolioAssetHistory

            @BeforeEach
            fun setUp() {
                current = PortfolioAssetHistory()
                current.lastUpdated = LocalDateTime.now()
                current.portfolioAsset = null
            }

            @Test
            @DisplayName("should return null")
            fun returnNull() {
                val result = portfolioService.getLatestPreviousAssetHistoryWithShares(current)
                assertNull(result)
            }
        }

        @Nested
        @DisplayName("when current history lacks lastUpdated")
        inner class WhenCurrentHistoryLacksLastUpdated {
            private lateinit var current: PortfolioAssetHistory

            @BeforeEach
            fun setUp() {
                val asset = getDummyAsset(1L)
                current = PortfolioAssetHistory(asset, false)
                current.lastUpdated = null
            }

            @Test
            @DisplayName("should return null")
            fun returnNull() {
                val result = portfolioService.getLatestPreviousAssetHistoryWithShares(current)
                assertNull(result)
            }
        }

        @Nested
        @DisplayName("when repository yields empty list")
        inner class WhenRepositoryYieldsEmptyList {
            private lateinit var current: PortfolioAssetHistory

            @BeforeEach
            fun setUp() {
                val asset = getDummyAsset(2L)
                current = PortfolioAssetHistory(asset, false)
                current.lastUpdated = LocalDateTime.now()
                `when`(
                    portfolioAssetHistoryRepository.findLatestWithSharesBefore(any(Long::class.java), any(LocalDateTime::class.java))
                ).thenReturn(emptyList())
            }

            @Test
            @DisplayName("should return null")
            fun returnNull() {
                val result = portfolioService.getLatestPreviousAssetHistoryWithShares(current)
                assertNull(result)
            }
        }

        @Nested
        @DisplayName("when the repository returns ordered histories")
        inner class WhenTheRepositoryReturnsOrderedHistories {
            private lateinit var current: PortfolioAssetHistory

            @BeforeEach
            fun setUp() {
                val asset = getDummyAsset(3L)
                current = PortfolioAssetHistory(asset, false)
                current.lastUpdated = LocalDateTime.now()
                val older = PortfolioAssetHistory(asset, current.lastUpdated.minusMinutes(10)).apply {
                    shares = 1.0
                    setIdForTest(101L)
                }
                val newer = PortfolioAssetHistory(asset, current.lastUpdated.minusMinutes(5)).apply {
                    shares = 2.0
                    setIdForTest(102L)
                }
                `when`(
                    portfolioAssetHistoryRepository.findLatestWithSharesBefore(asset.id, current.lastUpdated)
                ).thenReturn(listOf(newer, older))
            }

            @Test
            @DisplayName("should return first element from ordered list")
            fun returnFirstElementFromOrderedList() {
                val result = portfolioService.getLatestPreviousAssetHistoryWithShares(current)
                assertNotNull(result)
                assertEquals(102L, result!!.id)
                assertEquals(2.0, result.shares)
            }
        }
    }

    @Nested
    @DisplayName("setPortfolioValueChange")
    inner class SetPortfolioValueChangeTests {
        @Nested
        @DisplayName("when previous is null")
        inner class WhenPreviousIsNull {
            private lateinit var current: PortfolioAssetHistory

            @BeforeEach
            fun setUp() {
                val asset = getDummyAsset(5L)
                current = PortfolioAssetHistory(asset, false)
                current.totalValueInDollars = 200.0
            }

            @Test
            @DisplayName("should set zero")
            fun setZero() {
                portfolioService.setPortfolioValueChange(null, current)

                assertEquals(0.0, current.valueChange)
                assertEquals(0.0, current.sharesChange)
            }
        }

        @Nested
        @DisplayName("when previous history is provided")
        inner class WhenPreviousProvided {
            private lateinit var previous: PortfolioAssetHistory
            private lateinit var current: PortfolioAssetHistory

            @BeforeEach
            fun setUp() {
                val asset: PortfolioAsset = getDummyAsset(6L)
                previous = PortfolioAssetHistory(asset, LocalDateTime.now().minusMinutes(15)).apply {
                    shares = 1.5
                    totalValueInDollars = 150.0
                }
                current = PortfolioAssetHistory(asset, false).apply {
                    shares = 2.0
                    totalValueInDollars = 210.0
                }
            }

            @Disabled
            @Test
            @DisplayName("should compute deltas")
            fun computeDeltas() {
                portfolioService.setPortfolioValueChange(previous, current)
                portfolioService.setPortfolioSharesChange(previous, current)

                assertEquals(60.0, current.valueChange, 1e-9)
                assertEquals(0.5, current.sharesChange, 1e-9)
            }
        }
    }

    @Nested
    @DisplayName("setPortfolioSharesChange")
    inner class SetPortfolioShareChangeTests {
        @Nested
        @DisplayName("when zero shares occur between nonzero histories")
        inner class WhenZeroSharesOccurBetweenNonzeroHistories {
            @Test
            @DisplayName("should calculate the change from the last nonzero shares")
            fun useLastNonzeroShares() {
                val asset = getDummyAsset(42L)

                val eightPm = LocalDateTime.now().withHour(20).withMinute(0).withSecond(0).withNano(0)
                val sevenPm = eightPm.minusHours(1)
                val sixPm = eightPm.minusHours(2)

                val current = PortfolioAssetHistory(asset, false).apply {
                    shares = 10.0
                    lastUpdated = eightPm
                }
                val zeroAtSeven = PortfolioAssetHistory(asset, sevenPm).apply { shares = 0.0 }
                val nonZeroAtSix = PortfolioAssetHistory(asset, sixPm).apply { shares = 5.0 }

                // Repository should exclude the 7pm zero from results; return the 6pm non-zero baseline
                `when`(
                    portfolioAssetHistoryRepository.findLatestWithSharesBefore(asset.id, eightPm)
                ).thenReturn(listOf(nonZeroAtSix))

                val previousWithShares = portfolioService.getLatestPreviousAssetHistoryWithShares(current)
                assertNotNull(previousWithShares)
                assertEquals(5.0, previousWithShares!!.shares, 1e-9)

                portfolioService.setPortfolioSharesChange(previousWithShares, current)
                assertEquals(5.0, current.sharesChange, 1e-9) // 10 - 5 = 5
            }
        }

        @Nested
        @DisplayName("when shares decrease")
        inner class WhenSharesDecrease {
            private lateinit var previous: PortfolioAssetHistory
            private lateinit var current: PortfolioAssetHistory

            @BeforeEach
            fun setUp() {
                val asset = getDummyAsset(43L)
                previous = PortfolioAssetHistory(asset, LocalDateTime.now().minusMinutes(10)).apply {
                    shares = 10.0
                }
                current = PortfolioAssetHistory(asset, false).apply {
                    shares = 4.0
                }
            }

            @Test
            @DisplayName("should set sharesChange to 0")
            fun setSharesChangeTo0() {
                portfolioService.setPortfolioSharesChange(previous, current)

                assertEquals(0.0, current.sharesChange, 1e-9)
            }
        }
    }

    private fun getDummyAsset(id: Long): PortfolioAsset {
        val currency = Currency().apply {
            currencyCode = "DUMMY"
            name = "DUMMY"
            value = 1.0
        }
        val portfolio = Portfolio().apply { this.id = 999L }
        return PortfolioAsset(portfolio, currency, 0.0, 0.0).apply { this.id = id }
    }

    private fun PortfolioAssetHistory.setIdForTest(id: Long) {
        val field = this.javaClass.superclass.getDeclaredField("id")
        field.isAccessible = true
        field.set(this, id)
    }
}
