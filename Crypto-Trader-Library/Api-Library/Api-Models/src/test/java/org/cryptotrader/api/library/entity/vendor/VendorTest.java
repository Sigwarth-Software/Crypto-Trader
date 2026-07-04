package org.cryptotrader.api.library.entity.vendor;

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Vendor")
public class VendorTest extends CryptoTraderTest {
    private Vendor testVendor;
    @BeforeEach
    void setUp() {
        this.testVendor = new Vendor("Test Vendor", 0.1);
    }

    @Nested
    @DisplayName("Price Adjustment")
    class PriceAdjustment {
        @Test
        @DisplayName("Should adjust prices")
        public void testShouldAdjustPrices() {
            double price = 100;
            double expectedPrice = 110;
            double actualPrice = testVendor.getAdjustedPrice(price);
            assertEquals(expectedPrice, actualPrice);
        }
    }
}
