package org.cryptotrader.data.library.entity.currency

import java.io.File
import org.antlr.v4.runtime.misc.Utils.writeFile
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SupportedCurrencies")
@Disabled
@UnitSuite
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
    @DisplayName("loadCurrenciesFromJson")
    inner class StaticLoad {
        @Nested
        @DisplayName("when system property is false")
        inner class WhenSystemPropertyIsFalse {
            @BeforeEach
            fun setUp() {
                System.setProperty("org.cryptotrader.load.currency", "false")
            }

            @Test
            @DisplayName("should skip static load")
            fun skipStaticLoad() {
                SupportedCurrencies.loadCurrenciesFromJson()
                assertTrue(SupportedCurrencies.SUPPORTED_CURRENCIES.isEmpty())
            }
        }
    }
}
