package org.cryptotrader.api.controller

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("CryptoTraderController")
@Tag("controller")
@DisplayName("Root API Controller")
class CryptoTraderControllerTest : CryptoTraderTest() {

    private lateinit var controller: CryptoTraderController

    @BeforeEach
    fun setUp() {
        controller = CryptoTraderController()
    }

    @Nested
    @Tag("initialization")
    @DisplayName("Initialization")
    inner class Initialization {
        @Test
        @DisplayName("Should construct controller")
        fun initialization_Constructs() { }
    }
}
