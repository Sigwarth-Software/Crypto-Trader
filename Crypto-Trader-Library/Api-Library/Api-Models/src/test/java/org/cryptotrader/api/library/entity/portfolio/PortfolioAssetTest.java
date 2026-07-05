package org.cryptotrader.api.library.entity.portfolio;

import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Portfolio Asset")
public class PortfolioAssetTest extends CryptoTraderTest {
    private Currency testCurrency;
    private PortfolioAsset testAsset;
    @BeforeEach
    void setUp() {
        this.testCurrency = Currency.builder()
                .name("Test Coin")
                .currencyCode("TEST")
                .urlPath(Currency.TESTING_URL)
                .value(100)
                .build();
        this.testAsset = new PortfolioAsset(this.testCurrency, 0, 0);
    }

    @Nested
    @DisplayName("Trading Rules")
    class TradingRules {
        @Test
        @DisplayName("Should be sellable with shares")
        public void testShouldBeSellableWithShares() {
            testAsset.setShares(1);
            testAsset.setTargetPrice(100);
            boolean expected = true;
            assertEquals(expected, testAsset.canSell());
        }

        @Test
        @DisplayName("Should not be sellable without shares")
        public void testShouldNotBeSellableWithoutShares() {
            testAsset.setShares(0);
            testAsset.setTargetPrice(100);
            boolean expected = false;
            assertEquals(expected, testAsset.canSell());
        }

        @Test
        @DisplayName("Should be buyable with wallet dollars")
        public void testShouldBeBuyableWithWalletDollars() {
            testAsset.setAssetWalletDollars(100);
            testAsset.setTargetPrice(100);
            boolean expected = true;
            assertEquals(expected, testAsset.canBuy());
        }

        @Test
        @DisplayName("Should not be buyable with no wallet dollars")
        public void testShouldNotBeBuyableWithNoWalletDollars() {
            testAsset.setAssetWalletDollars(0);
        }
    }

    @Nested
    @DisplayName("Value Calculations")
    class ValueCalculations {
        @Test
        @DisplayName("Should calculate values of shares")
        public void testShouldCalculateValuesOfShares() {
            testAsset.setShares(1);
            testAsset.setTargetPrice(100);
            testAsset.updateValues();
            assertEquals(100, testAsset.getSharesValueInDollars());

            testAsset.setShares(2);
            testAsset.updateValues();
            assertEquals(200, testAsset.getSharesValueInDollars());
        }

        @Test
        @DisplayName("Should calculate total value of a wallet")
        public void testShouldCalculateTotalValueOfWallet() {
            testAsset.setAssetWalletDollars(100);
            testAsset.setTargetPrice(100);
            testAsset.setShares(1);
            testAsset.updateValues();
            assertEquals(200, testAsset.getTotalValueInDollars());
        }
    }
}
