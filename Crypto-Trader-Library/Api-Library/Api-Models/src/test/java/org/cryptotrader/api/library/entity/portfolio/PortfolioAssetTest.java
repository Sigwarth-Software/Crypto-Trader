package org.cryptotrader.api.library.entity.portfolio;

import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest;
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PortfolioAsset")
@UnitSuite
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
    @DisplayName("canSell")
    class CanSell {
        @Nested
        @DisplayName("when the asset has shares")
        class WhenTheAssetHasShares {
            @BeforeEach
            void setUp() throws Exception {
                testAsset.setShares(1);
                testAsset.setTargetPrice(100);
            }

            @Test
            @DisplayName("should be sellable with shares")
            public void beSellableWithShares() {
                boolean expected = true;
                assertEquals(expected, testAsset.canSell());
            }
        }

        @Nested
        @DisplayName("when the asset has no shares")
        class WhenTheAssetHasNoShares {
            @BeforeEach
            void setUp() throws Exception {
                testAsset.setShares(0);
                testAsset.setTargetPrice(100);
            }

            @Test
            @DisplayName("should not be sellable without shares")
            public void notBeSellableWithoutShares() {
                boolean expected = false;
                assertEquals(expected, testAsset.canSell());
            }
        }
    }

    @Nested
    @DisplayName("canBuy")
    class CanBuy {
        @Nested
        @DisplayName("when the asset wallet has dollars")
        class WhenTheAssetWalletHasDollars {
            @BeforeEach
            void setUp() throws Exception {
                testAsset.setAssetWalletDollars(100);
                testAsset.setTargetPrice(100);
            }

            @Test
            @DisplayName("should be buyable with wallet dollars")
            public void beBuyableWithWalletDollars() {
                boolean expected = true;
                assertEquals(expected, testAsset.canBuy());
            }
        }


    }

    @Nested
    @DisplayName("updateValues")
    class ValueCalculations {
        @Test
        @DisplayName("should calculate values of shares")
        public void calculateValuesOfShares() {
            testAsset.setShares(1);
            testAsset.setTargetPrice(100);
            testAsset.updateValues();
            assertEquals(100, testAsset.getSharesValueInDollars());

            testAsset.setShares(2);
            testAsset.updateValues();
            assertEquals(200, testAsset.getSharesValueInDollars());
        }

        @Test
        @DisplayName("should calculate total value of a wallet")
        public void calculateTotalValueOfAWallet() {
            testAsset.setAssetWalletDollars(100);
            testAsset.setTargetPrice(100);
            testAsset.setShares(1);
            testAsset.updateValues();
            assertEquals(200, testAsset.getTotalValueInDollars());
        }
    }
}
