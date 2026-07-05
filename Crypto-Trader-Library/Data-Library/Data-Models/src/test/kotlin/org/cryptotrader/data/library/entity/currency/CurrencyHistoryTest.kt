package org.cryptotrader.data.library.entity.currency

import org.cryptotrader.data.library.entity.currency.CurrencyHistory
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("CurrencyHistory")
@Tag("entity")
@DisplayName("Currency History Entity")
class CurrencyHistoryTest : CryptoTraderTest() {

    private lateinit var currencyHistory: CurrencyHistory

    @BeforeEach
    fun setUp() {
        this.currencyHistory = CurrencyHistory()
    }
}
