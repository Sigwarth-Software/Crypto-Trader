package org.cryptotrader.data.library.components

import org.cryptotrader.data.library.entity.currency.Currency
import org.cryptotrader.data.library.component.CurrencyDataRetriever
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("CurrencyDataRetriever")
@Tag("component")
@DisplayName("Currency Data Retriever")
class CurrencyDataRetrieverTest : CryptoTraderTest() {

    private lateinit var currencyDataRetriever: CurrencyDataRetriever

    @BeforeEach
    fun setUp() {
        this.currencyDataRetriever = CurrencyDataRetriever()
    }

    @Nested
    @Tag("getUpdatedCurrencies")
    @DisplayName("Get Updated Currencies")
    inner class GetUpdatedCurrencies {
        @Test
        @DisplayName("Should return only supported currencies with values")
        fun getUpdatedCurrencies_ReturnsFilteredSupported() {
            val currenciesMap: Map<String, Currency> = currencyDataRetriever.getUpdatedCurrencies()
            assertNotNull(currenciesMap)
            assertTrue(currenciesMap.isNotEmpty())
            for (currencyValue in currenciesMap.values) {
                assertTrue(currencyValue.value > 0.0)
            }
        }
    }

    @Nested
    @Tag("getCurrencyMap")
    @DisplayName("Get Currency Map")
    inner class GetCurrencyMap {
        @Test
        @DisplayName("Should parse API response into currency map")
        fun getCurrencyMap_ParsesResponse() {
            val currenciesMap: Map<String, Double> = currencyDataRetriever.getCurrencyMap();
            assertNotNull(currenciesMap)
            assertTrue(currenciesMap.isNotEmpty())
            for (currencyValue in currenciesMap.values) {
                assertTrue(currencyValue > 0.0)
            }
        }
    }
}
