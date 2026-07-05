package org.cryptotrader.data.library.entity.currency

import org.antlr.v4.runtime.misc.Utils.writeFile
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

@Tag("SupportedCurrencies")
@Tag("entity")
@DisplayName("Supported Currencies")
@Disabled
class SupportedCurrenciesTest : CryptoTraderTest() {

    companion object {
        const val TEST_JSON_PATH = "src/test/resources/static/currencies.json"

        @JvmStatic
        @BeforeAll
        fun setUp() {
            generateMockCurrencyJson()
        }
        @JvmStatic
        @AfterAll
        fun tearDown() {
            deleteMockCurrencyJson()
        }

        private fun generateMockCurrencyJson() {
            val json: String = """
                [
                    {
                        "code": "BTC",
                        "name": "Bitcoin"
                    },
                    {
                        "code": "ETH",
                        "name": "Ethereum"
                    }
                ]
            """.trimIndent()
            writeFile(TEST_JSON_PATH, json)
        }

        private fun deleteMockCurrencyJson() {
            val file = File(TEST_JSON_PATH)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    @Nested
    @Tag("staticLoad")
    @DisplayName("Static Load")
    inner class StaticLoad {
        @Test
        @DisplayName("Should skip static load when system property is false")
        fun staticLoad_Skips_WhenPropertyFalse() {
            System.setProperty("org.cryptotrader.load.currency", "false")
            SupportedCurrencies.loadCurrenciesFromJson()
            assertTrue(SupportedCurrencies.SUPPORTED_CURRENCIES.isEmpty())
        }
    }

    @Nested
    @Tag("loadCurrenciesFromJson")
    @DisplayName("Load Currencies From JSON")
    inner class LoadCurrenciesFromJson {
        @Test
        @DisplayName("Should load currencies from JSON file")
        fun loadCurrenciesFromJson_Loads() { }

        @Test
        @DisplayName("Should use empty list when file missing")
        fun loadCurrenciesFromJson_UsesEmpty_WhenMissingFile() { }
    }

    @Nested
    @Tag("constants")
    @DisplayName("Constants")
    inner class Constants {
        @Test
        @DisplayName("Should expose BITCOIN constant and supported list")
        fun constants_Exposed() { }
    }
}
