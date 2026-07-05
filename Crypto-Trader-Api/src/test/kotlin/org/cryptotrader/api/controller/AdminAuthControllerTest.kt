package org.cryptotrader.api.controller

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("AdminAuthController")
@Tag("controller")
@DisplayName("Admin Authentication Controller")
class AdminAuthControllerTest : CryptoTraderTest() {

    private lateinit var controller: AdminAuthController

    @BeforeEach
    fun setUp() {
        this.controller = AdminAuthController()
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
