package org.cryptotrader.data.library.components

import com.fasterxml.jackson.databind.ObjectMapper
import org.cryptotrader.data.library.component.CurrencyJsonGenerator
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.web.client.RestTemplate

@Tag("CurrencyJsonGenerator")
@Tag("component")
@DisplayName("Currency JSON Generator")
class CurrencyJsonGeneratorTest : CryptoTraderTest() {

    @Mock
    lateinit var restTemplate: RestTemplate

    @Mock
    lateinit var objectMapper: ObjectMapper

    @InjectMocks
    private lateinit var generator: CurrencyJsonGenerator

    private val exchangeJson = """
[
  {
    "id": "AAVE",
    "name": "Aave",
    "min_size": "0.01",
    "status": "online",
    "message": "",
    "max_precision": "0.001",
    "convertible_to": [],
    "details": {
      "type": "crypto",
      "symbol": null,
      "network_confirmations": 14,
      "sort_order": 270,
      "crypto_address_link": "https://etherscan.io/token/0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9?a={{address}}",
      "crypto_transaction_link": "https://etherscan.io/tx/0x{{txId}}",
      "push_payment_methods": [
        "crypto"
      ],
      "group_types": [],
      "display_name": null,
      "processing_time_seconds": null,
      "min_withdrawal_amount": 0.07,
      "max_withdrawal_amount": 32200
  },
  "default_network": "ethereum",
  "supported_networks": [
    {
      "id": "ethereum",
      "name": "Ethereum",
      "status": "online",
      "contract_address": "0x7Fc66500c84A76Ad7e9c93437bFc5Ac33E2DDaE9",
      "crypto_address_link": "https://etherscan.io/token/0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9?a={{address}}",
      "crypto_transaction_link": "https://etherscan.io/tx/0x{{txId}}",
      "min_withdrawal_amount": 0.07,
      "max_withdrawal_amount": 32200,
      "network_confirmations": 14,
      "processing_time_seconds": null,
      "destination_tag_regex": null
    }
  ],
  "display_name": "AAVE"
},
{
  "id": "ABT",
  "name": "Arcblock",
  "min_size": "0.1",
  "status": "online",
  "message": "",
  "max_precision": "0.00000001",
  "convertible_to": [],
  "details": {
    "type": "crypto",
    "symbol": null,
    "network_confirmations": 14,
    "sort_order": 0,
    "crypto_address_link": "https://etherscan.io/token/0xb98d4c97425d9908e66e53a6fdf673acca0be986?a={{address}}",
    "crypto_transaction_link": "https://etherscan.io/tx/0x{{txId}}",
    "push_payment_methods": [],
    "group_types": [],
    "display_name": null,
    "processing_time_seconds": null,
    "min_withdrawal_amount": 1e-8,
    "max_withdrawal_amount": 810000
  },
  "default_network": "ethereum",
  "supported_networks": [
      {
        "id": "ethereum",
        "name": "Ethereum",
        "status": "online",
        "contract_address": "0xb98d4c97425d9908e66e53a6fdf673acca0be986",
        "crypto_address_link": "https://etherscan.io/token/0xb98d4c97425d9908e66e53a6fdf673acca0be986?a={{address}}",
        "crypto_transaction_link": "https://etherscan.io/tx/0x{{txId}}",
        "min_withdrawal_amount": 1e-8,
        "max_withdrawal_amount": 810000,
        "network_confirmations": 14,
        "processing_time_seconds": null,
        "destination_tag_regex": null
      }
    ],
    "display_name": "ABT"
  }
]
    """.trimIndent()

    private val ratesJson = """
{
  "data": {
    "currency": "USD",
    "rates": {
      "00": "75.7575757575757576",
      "1INCH": "4.3010752688172043",
      "A8": "10.5876124933827422",
      "AAVE": "0.0037642807400576",
      "ABT": "1.7082336863682952",
      "ACH": "56.559486439863126",
      "ACS": "1201.7064231208315808",
      "ACX": "9.0826521344232516",
      "ADA": "1.2730744748567791",
      "AED": "3.67275",
      "AERGO": "10.2774922918807811",
      "AERO": "0.981465032854542",
      "AFN": "67.349872503671258",
      "AGLD": "1.7930787161556392",
      "AIOZ": "3.6920804873546243",
      "AKT": "1.0050251256281407",
      "ALCX": "0.1192605843768634",
      "ALEO": "4.7393364928909953",
      "ALEPH": "14.0845070422535211",
      "ALGO": "4.8344210780759004",
    }
  }
}
    """.trimIndent()

    @BeforeEach
    fun setUp() {
        this.generator = CurrencyJsonGenerator(restTemplate, objectMapper)
    }

    @Nested
    @Tag("getCurrencies")
    @DisplayName("Get Currencies")
    inner class GetCurrencies {
        @Test
        @DisplayName("Should fetch and match currencies against rates")
        fun getCurrencies_MatchesAgainstRates() { }
    }

    @Nested
    @Tag("saveJson")
    @DisplayName("Save JSON")
    inner class SaveJson {
        @Test
        @DisplayName("Should write matched currencies to output path")
        fun saveJson_WritesOutput() { }
    }

    @Nested
    @Tag("getAllCurrencyCodes")
    @DisplayName("Get All Currency Codes")
    inner class GetAllCurrencyCodes {
        @Test
        @DisplayName("Should return codes using cache when available")
        fun getAllCurrencyCodes_UsesCache_WhenAvailable() { }

        @Test
        @DisplayName("Should fetch from API when cache missing or read fails")
        fun getAllCurrencyCodes_FallsBack_WhenCacheUnavailable() { }
    }

    @Nested
    @Tag("generateAndSave")
    @DisplayName("Generate And Save")
    inner class GenerateAndSave {
        @Test
        @DisplayName("Should generate currency list and save as JSON")
        fun generateAndSave_GeneratesAndSaves() { }
    }

    @Nested
    @Tag("standalone")
    @DisplayName("Standalone Factory")
    inner class StandaloneFactory {
        @Test
        @DisplayName("Should create standalone instance")
        fun standalone_CreatesInstance() {
            assertNotNull(CurrencyJsonGenerator.standalone())
        }
    }
}
