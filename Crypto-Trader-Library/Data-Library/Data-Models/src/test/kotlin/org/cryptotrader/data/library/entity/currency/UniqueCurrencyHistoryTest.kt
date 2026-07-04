package org.cryptotrader.data.library.entity.currency

import org.cryptotrader.data.library.entity.currency.UniqueCurrencyHistory
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("UniqueCurrencyHistory")
@Tag("entity")
@DisplayName("Unique Currency History Entity")
class UniqueCurrencyHistoryTest : CryptoTraderTest() {

    private lateinit var uniqueCurrencyHistory: UniqueCurrencyHistory

    @BeforeEach
    fun setUp() {
        this.uniqueCurrencyHistory = UniqueCurrencyHistory()
    }
}
