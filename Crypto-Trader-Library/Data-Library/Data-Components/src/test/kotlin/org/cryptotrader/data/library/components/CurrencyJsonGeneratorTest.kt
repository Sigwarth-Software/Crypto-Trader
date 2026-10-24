package org.cryptotrader.data.library.components

import org.cryptotrader.data.library.component.CurrencyJsonGenerator
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("CurrencyJsonGenerator")
@UnitSuite
class CurrencyJsonGeneratorTest : CryptoTraderTest() {


    @Nested
    @DisplayName("standalone")
    inner class StandaloneFactory {
        @Test
        @DisplayName("should create standalone instance")
        fun createStandaloneInstance() {
            assertNotNull(CurrencyJsonGenerator.standalone())
        }
    }
}
