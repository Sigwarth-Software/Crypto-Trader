package org.cryptotrader.data.library.entity.currency.builder

import org.cryptotrader.data.library.entity.currency.builder.CurrencyBuilder
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("CurrencyBuilder")
@Tag("entity")
@DisplayName("Currency Builder")
class CurrencyBuilderTest : CryptoTraderTest() {

    private lateinit var builder: CurrencyBuilder

    @BeforeEach
    fun setUp() {
        this.builder = CurrencyBuilder()
    }

    @Nested
    @Tag("name")
    @DisplayName("Name")
    inner class NameSetter {
        @Test
        @DisplayName("Should set name")
        fun name_Sets() { }
    }

    @Nested
    @Tag("currencyCode")
    @DisplayName("Currency Code")
    inner class CurrencyCodeSetter {
        @Test
        @DisplayName("Should set currency code")
        fun currencyCode_Sets() { }
    }

    @Nested
    @Tag("urlPath")
    @DisplayName("URL Path")
    inner class UrlPathSetter {
        @Test
        @DisplayName("Should set URL path")
        fun urlPath_Sets() { }
    }

    @Nested
    @Tag("value")
    @DisplayName("Value")
    inner class ValueSetter {
        @Test
        @DisplayName("Should set value")
        fun value_Sets() { }
    }

    @Nested
    @Tag("lastUpdated")
    @DisplayName("Last Updated")
    inner class LastUpdatedSetter {
        @Test
        @DisplayName("Should set lastUpdated")
        fun lastUpdated_Sets() { }
    }

    @Nested
    @Tag("build")
    @DisplayName("Build")
    inner class Build {
        @Test
        @DisplayName("Should build Currency from set fields")
        fun build_Builds() { }
    }
}
