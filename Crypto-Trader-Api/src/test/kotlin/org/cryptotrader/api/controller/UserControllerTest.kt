package org.cryptotrader.api.controller

import org.cryptotrader.api.library.services.PortfolioService
import org.cryptotrader.api.library.services.ProductUserService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mock

@Tag("UserController")
@Tag("controller")
@DisplayName("User Controller")
class UserControllerTest : CryptoTraderTest() {

    @Mock lateinit var productUserService: ProductUserService
    @Mock lateinit var portfolioService: PortfolioService

    private lateinit var controller: UserController

    @BeforeEach
    fun setUp() {
        this.controller = UserController(productUserService, portfolioService)
    }

    @Nested
    @Tag("initialization")
    @DisplayName("Initialization")
    inner class Initialization {
        @Test
        @DisplayName("Should construct controller")
        fun initialization_Constructs() {
            assertNotNull(controller)
        }
    }
}
