package org.cryptotrader.api.library.trade;

import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;
import org.cryptotrader.api.library.model.trade.AssetTrader;
import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest;
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("AssetTrader")
@UnitSuite
public class AssetTraderTest extends CryptoTraderTest {
    private Currency testCurrency;
    private PortfolioAsset testAsset;
    private AssetTrader testTrader;
    @BeforeEach
    void setUp() {
        this.testCurrency = Currency.builder()
                                    .name("Test Coin")
                                    .currencyCode("TEST")
                                    .urlPath(Currency.TESTING_URL)
                                    .value(100)
                                    .build();
        this.testAsset = new PortfolioAsset(this.testCurrency,0, 0);
        this.testTrader = new AssetTrader(this.testAsset);
    }

    @Nested
    @DisplayName("trade")
    class TradingDecisions {
        @Nested
        @DisplayName("when the price increases above the target")
        class WhenThePriceIncreasesAboveTheTarget {
            @BeforeEach
            void setUp() throws Exception {
                testAsset.setShares(1);
                testAsset.setTargetPrice(100);
            }

            @Test
            @DisplayName("should sell at an increased price")
            public void sellAtAnIncreasedPrice() {
                boolean expected = false;
                assertEquals(expected, testTrader.trade());
                testCurrency.setValue(101);
                expected = true;
                assertEquals(expected, testTrader.trade());
            }
        }

        @Nested
        @DisplayName("when the price decreases below the target")
        class WhenThePriceDecreasesBelowTheTarget {
            @BeforeEach
            void setUp() throws Exception {
                testAsset.setShares(0);
                testAsset.setAssetWalletDollars(100);
                testAsset.setTargetPrice(100);
            }

            @Test
            @DisplayName("should buy at a decreased price")
            public void buyAtADecreasedPrice() {
                boolean expected = false;
                assertEquals(expected, testTrader.trade());
                testCurrency.setValue(99);
                expected = true;
                assertEquals(expected, testTrader.trade());
            }
        }
    }

    @Nested
    @DisplayName("sell")
    class Sell {
        @Test
        @DisplayName("should empty the shares")
        public void emptyTheShares() {
            testAsset.setShares(1);
            testAsset.setTargetPrice(100);
            testTrader.sell(testCurrency.getValue());
            assertEquals(0, testAsset.getShares());
        }
    }

    @Nested
    @DisplayName("buy")
    class Buy {
        @Test
        @DisplayName("should add shares")
        public void addShares() {
            testAsset.setShares(0);
            testAsset.setAssetWalletDollars(100);
            testAsset.setTargetPrice(100);
            testTrader.buy(testCurrency.getValue());
            assertEquals(1, testAsset.getShares());
        }
    }
}
