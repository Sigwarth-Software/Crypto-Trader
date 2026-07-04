package org.cryptotrader.api.library.entity.vendor

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("SupportedVendors")
@Tag("entity")
@DisplayName("Supported Vendors")
class SupportedVendorsTest : CryptoTraderTest() {

    @Nested
    @Tag("constants")
    @DisplayName("Constants")
    inner class Constants {
        @Test
        @DisplayName("Should expose non-null vendors")
        fun constants_NotNull() {
            SupportedVendors.VENDOR_LIST.forEach {
                assertNotNull(it)
            }
        }
    }
}
