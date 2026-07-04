package org.cryptotrader.data.library.entity.news

import org.cryptotrader.data.library.entity.news.NewsSentiment
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("NewsSentiment")
@Tag("entity")
@DisplayName("News Sentiment Entity")
class NewsSentimentTest : CryptoTraderTest() {

    private lateinit var newsSentiment: NewsSentiment

    @BeforeEach
    fun setUp() {
        this.newsSentiment = NewsSentiment()
    }

    @Nested
    @Tag("builder")
    @DisplayName("Builder")
    inner class BuilderMethod {
        @Test
        @DisplayName("Should provide builder for NewsSentiment")
        fun builder_ProvidesBuilder() { }
    }

    @Nested
    @Tag("calculateWeightedScore")
    @DisplayName("Calculate Weighted Score")
    inner class CalculateWeightedScore {
        @Test
        @DisplayName("Should compute compositeScore * cryptoRelevance")
        fun calculateWeightedScore_ComputesProduct() { }
    }
}
