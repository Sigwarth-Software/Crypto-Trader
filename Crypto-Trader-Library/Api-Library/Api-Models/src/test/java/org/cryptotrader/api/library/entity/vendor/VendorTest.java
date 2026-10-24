package org.cryptotrader.api.library.entity.vendor;

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest;
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Vendor")
@UnitSuite
public class VendorTest extends CryptoTraderTest {
    private Vendor testVendor;
    @BeforeEach
    void setUp() {
        this.testVendor = new Vendor("Test Vendor", 0.1);
    }

    @Nested
    @DisplayName("getAdjustedPrice")
    class PriceAdjustment {
        @Test
        @DisplayName("should adjust prices")
        public void adjustPrices() {
            final double price = 100;
            final double expectedPrice = 110;
            final double actualPrice = testVendor.getAdjustedPrice(price);
            assertEquals(expectedPrice, actualPrice);
        }
    }
}
