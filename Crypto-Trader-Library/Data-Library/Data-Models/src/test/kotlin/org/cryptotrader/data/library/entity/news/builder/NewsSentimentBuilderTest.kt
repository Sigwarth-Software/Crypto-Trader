package org.cryptotrader.data.library.entity.news.builder

import org.cryptotrader.data.library.entity.news.builder.NewsSentimentBuilder
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("NewsSentimentBuilder")
@Tag("entity")
@DisplayName("News Sentiment Builder")
class NewsSentimentBuilderTest : CryptoTraderTest() {

    private lateinit var builder: NewsSentimentBuilder

    @BeforeEach
    fun setUp() {
        this.builder = NewsSentimentBuilder()
    }

    @Nested
    @Tag("articleId")
    @DisplayName("Article Id")
    inner class ArticleId {
        @Test
        @DisplayName("Should set article id")
        fun articleId_Sets() { }
    }

    @Nested
    @Tag("title")
    @DisplayName("Title")
    inner class Title {
        @Test
        @DisplayName("Should set title")
        fun title_Sets() { }
    }

    @Nested
    @Tag("publishedDate")
    @DisplayName("Published Date")
    inner class PublishedDate {
        @Test
        @DisplayName("Should set published date from string")
        fun publishedDate_FromString_Sets() { }

        @Test
        @DisplayName("Should set published date from LocalDateTime")
        fun publishedDate_FromLocalDateTime_Sets() { }
    }

    @Nested
    @Tag("source")
    @DisplayName("Source")
    inner class Source {
        @Test
        @DisplayName("Should set source")
        fun source_Sets() { }
    }

    @Nested
    @Tag("url")
    @DisplayName("URL")
    inner class Url {
        @Test
        @DisplayName("Should set url")
        fun url_Sets() { }
    }

    @Nested
    @Tag("scores")
    @DisplayName("Scores")
    inner class Scores {
        @Test
        @DisplayName("Should set positive score")
        fun positiveScore_Sets() { }

        @Test
        @DisplayName("Should set neutral score")
        fun neutralScore_Sets() { }

        @Test
        @DisplayName("Should set negative score")
        fun negativeScore_Sets() { }

        @Test
        @DisplayName("Should set composite score")
        fun compositeScore_Sets() { }

        @Test
        @DisplayName("Should set crypto relevance")
        fun cryptoRelevance_Sets() { }
    }

    @Nested
    @Tag("lastUpdated")
    @DisplayName("Last Updated")
    inner class LastUpdated {
        @Test
        @DisplayName("Should set last updated from LocalDateTime")
        fun lastUpdated_FromLocalDateTime_Sets() { }

        @Test
        @DisplayName("Should set last updated from string")
        fun lastUpdated_FromString_Sets() { }
    }

    @Nested
    @Tag("build")
    @DisplayName("Build")
    inner class Build {
        @Test
        @DisplayName("Should build NewsSentiment from set fields")
        fun build_Builds() { }
    }
}
