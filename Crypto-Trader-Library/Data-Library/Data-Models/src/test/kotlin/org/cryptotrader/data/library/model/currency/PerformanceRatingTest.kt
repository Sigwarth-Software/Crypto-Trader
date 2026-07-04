package org.cryptotrader.data.library.model.currency

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("PerformanceRating")
@Tag("model")
@DisplayName("Performance Rating")
class PerformanceRatingTest : CryptoTraderTest() {

    @Nested
    @DisplayName("fromValues")
    inner class FromValues {
        @Test
        @DisplayName("Should return rating with valid values")
        fun fromValues_ReturnValidRating_WithValidValues() {
            var rating = PerformanceRating.fromValues(100.0, 100.0)
            Assertions.assertEquals(rating, PerformanceRating.NEUTRAL)

            rating = PerformanceRating.fromValues(100.0, 99.0)
            Assertions.assertEquals(rating, PerformanceRating.DOWN)

            rating = PerformanceRating.fromValues(99.0, 100.0)
            Assertions.assertEquals(rating, PerformanceRating.UP)
        }
    }
}