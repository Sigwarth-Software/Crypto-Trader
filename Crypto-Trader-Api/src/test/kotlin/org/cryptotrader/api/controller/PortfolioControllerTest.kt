package org.cryptotrader.api.controller

import org.cryptotrader.api.library.entity.portfolio.Portfolio
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.AuthContextService
import org.cryptotrader.api.library.services.PortfolioService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@Tag("PortfolioController")
@Tag("controller")
@DisplayName("Portfolio Controller")
class PortfolioControllerTest : CryptoTraderTest() {

    @Mock lateinit var authContextService: AuthContextService
    @Mock lateinit var portfolioService: PortfolioService

    private lateinit var controller: PortfolioController
    private lateinit var testPortfolio: Portfolio
    private lateinit var testUser: ProductUser
    @BeforeEach
    fun setUp() {
        this.testPortfolio = Portfolio.builder().build()
        testPortfolio.id = 1L
        this.testUser = ProductUser.builder()
                                   .email("test@test.com")
                                   .safePassword("password")
                                   .build()
        this.testUser.id = 1L
        this.testUser.portfolio = this.testPortfolio
        this.testPortfolio.user = testUser
        this.controller = PortfolioController(authContextService, portfolioService)
    }

    @Nested
    @Tag("getPortfolio")
    @DisplayName("Get Portfolio")
    inner class GetPortfolio {
        @Test
        @DisplayName("Should return portfolio for authenticated user")
        fun getPortfolio_Returns_WhenAuthenticated() {
            `when`(portfolioService.getPortfolioByUserId(testUser.id)).thenReturn(testPortfolio)
            val response = controller.getPortfolio(testUser)
            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(testPortfolio.id, response.body?.id)
        }
    }

    @Nested
    @Tag("addPortfolioAsset")
    @DisplayName("Add Portfolio Asset")
    inner class AddPortfolioAsset {
        @Test
        @DisplayName("Should add asset to portfolio for authenticated user")
        fun addPortfolioAsset_Adds_WhenAuthenticated() { }

        @Test
        @DisplayName("Should return bad request when request missing")
        fun addPortfolioAsset_ReturnsBadRequest_WhenMissing() { }
    }

    @Nested
    @Tag("emptyPortfolio")
    @DisplayName("Empty Portfolio")
    inner class EmptyPortfolio {
        @Test
        @DisplayName("Should return whether portfolio is empty")
        fun emptyPortfolio_ReturnsFlag() { }
    }

    @Nested
    @Tag("getPortfolioHistory")
    @DisplayName("Get Portfolio History")
    inner class GetPortfolioHistory {
        @Test
        @DisplayName("Should return history or no content")
        fun getPortfolioHistory_ReturnsHistory_OrNoContent() { }
    }

    @Nested
    @Tag("getPortfolioAssetHistory")
    @DisplayName("Get Portfolio Asset History")
    inner class GetPortfolioAssetHistory {
        @Test
        @DisplayName("Should return asset history list")
        fun getPortfolioAssetHistory_ReturnsList() { }

        @Test
        @DisplayName("Should return asset history by currency")
        fun getPortfolioAssetHistoryByCurrency_ReturnsList() { }
    }

    @Nested
    @Tag("getPortfolioProfit")
    @DisplayName("Get Portfolio Profit")
    inner class GetPortfolioProfit {
        @Test
        @DisplayName("Should return portfolio profit")
        fun getPortfolioProfit_ReturnsProfit() { }

        @Test
        @DisplayName("Should return profit by currency")
        fun getPortfolioProfitByCurrency_ReturnsProfit() { }
    }
}
