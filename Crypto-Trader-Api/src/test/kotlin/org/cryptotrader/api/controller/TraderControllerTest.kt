package org.cryptotrader.api.controller

import org.cryptotrader.api.library.services.AuthContextService
import org.cryptotrader.api.library.services.PortfolioService
import org.cryptotrader.api.library.services.TraderService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

@Tag("TraderController")
@Tag("controller")
@DisplayName("Trader Controller")
class TraderControllerTest : CryptoTraderTest() {

    private lateinit var controller: TraderController
    private lateinit var traderService: TraderService
    private lateinit var authContextService: AuthContextService

    @BeforeEach
    fun setUp() {
        this.traderService = mock()
        this.authContextService = mock()
        this.controller = TraderController(this.traderService, this.authContextService)
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
