package org.cryptotrader.api.library.entity.vendor

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SupportedVendors")
@UnitSuite
class SupportedVendorsTest : CryptoTraderTest() {

    @Nested
    @DisplayName("constants")
    inner class Constants {
        @Test
        @DisplayName("should expose non-null vendors")
        fun exposeNonNullVendors() {
            SupportedVendors.VENDOR_LIST.forEach {
                assertNotNull(it)
            }
        }
    }
}
