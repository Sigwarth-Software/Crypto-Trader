package org.cryptotrader.api.controller

import org.cryptotrader.api.library.entity.portfolio.Portfolio
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.AuthContextService
import org.cryptotrader.api.library.services.PortfolioService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus

@DisplayName("PortfolioController")
@UnitSuite
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
        this.controller = PortfolioController(portfolioService)
    }

    @Nested
    @DisplayName("getPortfolio")
    inner class GetPortfolio {
        @Nested
        @DisplayName("when the user has a portfolio")
        inner class WhenAuthenticated {
            @BeforeEach
            fun setUp() {
                `when`(portfolioService.getPortfolioByUserId(testUser.id)).thenReturn(testPortfolio)
            }

            @Test
            @DisplayName("should return portfolio for authenticated user")
            fun returnPortfolioForAuthenticatedUser() {
                val response = controller.getPortfolio(testUser)
                assertEquals(HttpStatus.OK, response.statusCode)
                assertEquals(testPortfolio.id, response.body?.id)
            }
        }
    }
}
