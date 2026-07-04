package org.cryptotrader.api.library.services

import org.cryptotrader.data.library.entity.currency.Currency
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
import org.cryptotrader.data.library.services.CurrencyService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime

@Tag("PortfolioService")
@Tag("service")
@DisplayName("Portfolio Service")
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
    fun setup() {
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
    @Tag("getLatestPreviousAssetHistoryWithShares")
    @DisplayName("Get Latest Previous Asset History With Shares")
    inner class GetLatestPreviousAssetHistoryWithShares {

        @Test
        @DisplayName("Returns null when current history is null")
        fun getLatestPreviousAssetHistoryWithShares_ReturnsNull_CurrentHistoryIsNull() {
            val result = portfolioService.getLatestPreviousAssetHistoryWithShares(null)
            assertNull(result)
        }

        @Test
        @DisplayName("Returns null when current history lacks asset")
        fun getLatestPreviousAssetHistoryWithShares_ReturnsNull_LackingAssetHistory() {
            val current = PortfolioAssetHistory()
            current.lastUpdated = LocalDateTime.now()
            current.portfolioAsset = null
            val result = portfolioService.getLatestPreviousAssetHistoryWithShares(current)
            assertNull(result)
        }

        @Test
        @DisplayName("Returns null when current history lacks lastUpdated")
        fun getLatestPreviousAssetHistoryWithShares_ReturnsNull_WithoutTimeField() {
            val asset = getDummyAsset(1L)
            val current = PortfolioAssetHistory(asset, false)
            current.lastUpdated = null
            val result = portfolioService.getLatestPreviousAssetHistoryWithShares(current)
            assertNull(result)
        }

        @Test
        @DisplayName("Returns null when repository yields empty list")
        fun getLatestPreviousAssetHistoryWithShares_ReturnsNull_EmptyList() {
            val asset = getDummyAsset(2L)
            val current = PortfolioAssetHistory(asset, false)
            current.lastUpdated = LocalDateTime.now()

            `when`(
                portfolioAssetHistoryRepository.findLatestWithSharesBefore(any(Long::class.java), any(LocalDateTime::class.java))
            ).thenReturn(emptyList())

            val result = portfolioService.getLatestPreviousAssetHistoryWithShares(current)
            assertNull(result)
        }

        @Test
        @DisplayName("Returns first element from ordered list")
        fun getLatestPreviousAssetHistoryWithShares_ReturnsFirstAsset_OrderedList() {
            val asset = getDummyAsset(3L)
            val current = PortfolioAssetHistory(asset, false)
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

            val result = portfolioService.getLatestPreviousAssetHistoryWithShares(current)
            assertNotNull(result)
            assertEquals(102L, result!!.id)
            assertEquals(2.0, result.shares)
        }
    }

    @Nested
    @Tag("setPortfolioValueChange")
    @DisplayName("Set Portfolio Value Change")
    inner class SetPortfolioValueChangeTests {
        @Test
        @DisplayName("Sets zero when previous is null")
        fun setPortfolioValueChange_SetsZeroChange_PreviousIsNull() {
            val asset = getDummyAsset(5L)
            val current = PortfolioAssetHistory(asset, false)
            current.totalValueInDollars = 200.0

            portfolioService.setPortfolioValueChange(null, current)

            assertEquals(0.0, current.valueChange)
            assertEquals(0.0, current.sharesChange)
        }

        @Disabled
        @Test
        @DisplayName("Computes deltas when previous provided")
        fun setPortfolioValueChange_ComputesDelta_WithPreviousAsset() {
            val asset: PortfolioAsset = getDummyAsset(6L)
            val previous: PortfolioAssetHistory = PortfolioAssetHistory(asset, LocalDateTime.now().minusMinutes(15)).apply {
                shares = 1.5
                totalValueInDollars = 150.0
            }
            val current = PortfolioAssetHistory(asset, false).apply {
                shares = 2.0
                totalValueInDollars = 210.0
            }

            portfolioService.setPortfolioValueChange(previous, current)
            portfolioService.setPortfolioSharesChange(previous, current)

            assertEquals(60.0, current.valueChange, 1e-9)
            assertEquals(0.5, current.sharesChange, 1e-9)
        }
    }

    @Nested
    @Tag("setPortfolioShareChange")
    @DisplayName("Set Portfolio Share Change")
    inner class SetPortfolioShareChangeTests {
        @Test
        @DisplayName("Computes sharesChange from last non-zero shares ignoring zero in-between (6pm=5, 7pm=0, 8pm=10 -> 5)")
        fun setPortfolioShareChange_ComputesFromLastNonZero_IgnoresZeroInBetween() {
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

        @Test
        @DisplayName("Sets sharesChange to 0 when shares decreased (selling does not count)")
        fun setPortfolioShareChange_SetsZero_OnSellDecrease() {
            val asset = getDummyAsset(43L)

            val previous = PortfolioAssetHistory(asset, LocalDateTime.now().minusMinutes(10)).apply {
                shares = 10.0
            }
            val current = PortfolioAssetHistory(asset, false).apply {
                shares = 4.0
            }

            portfolioService.setPortfolioSharesChange(previous, current)

            assertEquals(0.0, current.sharesChange, 1e-9)
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
