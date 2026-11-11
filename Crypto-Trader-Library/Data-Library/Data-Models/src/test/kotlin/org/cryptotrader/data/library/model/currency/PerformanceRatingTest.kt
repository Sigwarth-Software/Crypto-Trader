package org.cryptotrader.data.library.model.currency

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("PerformanceRating")
@UnitSuite
class PerformanceRatingTest : CryptoTraderTest() {

    @Nested
    @DisplayName("fromValues")
    inner class FromValues {
        @Nested
        @DisplayName("when values are equal")
        inner class WhenValuesAreEqual {
            @Test
            @DisplayName("should return neutral")
            fun returnNeutral() {
                val rating = PerformanceRating.fromValues(100.0, 100.0)
                Assertions.assertEquals(rating, PerformanceRating.NEUTRAL)
            }
        }

        @Nested
        @DisplayName("when the value decreases")
        inner class WhenValueDecreases {
            @Test
            @DisplayName("should return down")
            fun returnDown() {
                val rating = PerformanceRating.fromValues(100.0, 99.0)
                Assertions.assertEquals(rating, PerformanceRating.DOWN)
            }
        }

        @Nested
        @DisplayName("when the value increases")
        inner class WhenValueIncreases {
            @Test
            @DisplayName("should return up")
            fun returnUp() {
                val rating = PerformanceRating.fromValues(99.0, 100.0)
                Assertions.assertEquals(rating, PerformanceRating.UP)
            }
        }
    }
}
