package org.cryptotrader.api.config

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("HttpConfig")
@Tag("config")
@DisplayName("HTTP Configuration")
class HttpConfigTest : CryptoTraderTest() {

    private lateinit var config: HttpConfig

    @BeforeEach
    fun setUp() {
        config = HttpConfig()
    }

    @Nested
    @Tag("initialization")
    @DisplayName("Initialization")
    inner class Initialization {
        @Test
        @DisplayName("Should construct configuration class")
        fun initialization_Constructs() { }
    }
}
