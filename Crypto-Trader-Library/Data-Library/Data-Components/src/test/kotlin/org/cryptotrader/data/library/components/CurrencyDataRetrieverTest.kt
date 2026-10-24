package org.cryptotrader.data.library.components

import org.cryptotrader.data.library.component.CurrencyDataRetriever
import org.cryptotrader.data.library.entity.currency.Currency
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.IntegrationSuite
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("CurrencyDataRetriever")
@IntegrationSuite
class CurrencyDataRetrieverTest : CryptoTraderTest() {

    private lateinit var currencyDataRetriever: CurrencyDataRetriever

    @BeforeEach
    fun setUp() {
        this.currencyDataRetriever = CurrencyDataRetriever()
    }

    @Nested
    @DisplayName("getUpdatedCurrencies")
    inner class GetUpdatedCurrencies {
        @Test
        @DisplayName("should return only supported currencies with values")
        fun returnOnlySupportedCurrenciesWithValues() {
            val currenciesMap: Map<String, Currency> = currencyDataRetriever.getUpdatedCurrencies()
            assertNotNull(currenciesMap)
            assertTrue(currenciesMap.isNotEmpty())
            for (currencyValue in currenciesMap.values) {
                assertTrue(currencyValue.value > 0.0)
            }
        }
    }

    @Nested
    @DisplayName("getCurrencyMap")
    inner class GetCurrencyMap {
        @Test
        @DisplayName("should parse API response into currency map")
        fun parseAPIResponseIntoCurrencyMap() {
            val currenciesMap: Map<String, Double> = currencyDataRetriever.getCurrencyMap();
            assertNotNull(currenciesMap)
            assertTrue(currenciesMap.isNotEmpty())
            for (currencyValue in currenciesMap.values) {
                assertTrue(currencyValue > 0.0)
            }
        }
    }
}
