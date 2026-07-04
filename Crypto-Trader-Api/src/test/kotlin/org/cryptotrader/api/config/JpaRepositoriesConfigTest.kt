package org.cryptotrader.api.config

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("JpaRepositoriesConfig")
@Tag("config")
@DisplayName("JPA Repositories Configuration")
class JpaRepositoriesConfigTest : CryptoTraderTest() {

    private lateinit var config: JpaRepositoriesConfig

    @BeforeEach
    fun setUp() {
        config = JpaRepositoriesConfig()
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
