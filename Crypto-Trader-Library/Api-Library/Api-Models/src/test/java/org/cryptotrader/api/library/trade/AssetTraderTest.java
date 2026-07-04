package org.cryptotrader.api.library.trade;


import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;
import org.cryptotrader.api.library.model.trade.AssetTrader;
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Asset Trader")
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
    @DisplayName("Trading Decisions")
    class TradingDecisions {
        @Test
        @DisplayName("Should sell at an increased price")
        public void testShouldSell() {
            testAsset.setShares(1);
            testAsset.setTargetPrice(100);
            boolean expected = false;
            assertEquals(expected, testTrader.trade());
            testCurrency.setValue(101);
            expected = true;
            assertEquals(expected, testTrader.trade());
        }

        @Test
        @DisplayName("Should buy at a decreased price")
        public void testShouldBuy() {
            testAsset.setShares(0);
            testAsset.setAssetWalletDollars(100);
            testAsset.setTargetPrice(100);
            boolean expected = false;
            assertEquals(expected, testTrader.trade());
            testCurrency.setValue(99);
            expected = true;
            assertEquals(expected, testTrader.trade());
        }
    }

    @Nested
    @DisplayName("Trade Execution")
    class TradeExecution {
        @Test
        @DisplayName("Selling should empty shares")
        public void testShouldEmptyShares() {
            testAsset.setShares(1);
            testAsset.setTargetPrice(100);
            testTrader.sell(testCurrency.getValue());
            assertEquals(0, testAsset.getShares());
        }

        @Test
        @DisplayName("Buying should add shares")
        public void testShouldAddShares() {
            testAsset.setShares(0);
            testAsset.setAssetWalletDollars(100);
            testAsset.setTargetPrice(100);
            testTrader.buy(testCurrency.getValue());
            assertEquals(1, testAsset.getShares());
        }
    }
}
