package org.cryptotrader.api

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("CryptoTraderApiApplication")
@Tag("application")
@DisplayName("Crypto-Trader API Application")
class CryptoTraderApiApplicationTest : CryptoTraderTest() {

    private lateinit var app: CryptoTraderApiApplication

    @BeforeEach
    fun setUp() {
        this.app = CryptoTraderApiApplication()
    }

    @Nested
    @Tag("main")
    @DisplayName("Main Entry Point")
    inner class MainEntryPoint {
        @Test
        @DisplayName("Should start Spring application")
        fun main_StartsApplication() {
            assertNotNull(app)
        }
    }
}
