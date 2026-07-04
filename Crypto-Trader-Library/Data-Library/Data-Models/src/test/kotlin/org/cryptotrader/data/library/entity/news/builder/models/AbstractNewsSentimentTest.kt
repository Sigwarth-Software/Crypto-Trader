package org.cryptotrader.data.library.entity.news.builder.models

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("AbstractNewsSentiment")
@Tag("entity")
@DisplayName("Abstract News Sentiment Builder Model")
class AbstractNewsSentimentTest : CryptoTraderTest() {

    @Nested
    @Tag("fluentSetters")
    @DisplayName("Fluent Setters")
    inner class FluentSetters {
        @Test
        @DisplayName("Should support articleId setter")
        fun articleId_Setter() { }

        @Test
        @DisplayName("Should support title setter")
        fun title_Setter() { }

        @Test
        @DisplayName("Should support publishedDate setters")
        fun publishedDate_Setters() { }

        @Test
        @DisplayName("Should support source setter")
        fun source_Setter() { }

        @Test
        @DisplayName("Should support url setter")
        fun url_Setter() { }

        @Test
        @DisplayName("Should support score setters")
        fun scores_Setters() { }

        @Test
        @DisplayName("Should support lastUpdated setters")
        fun lastUpdated_Setters() { }
    }

    @Nested
    @Tag("build")
    @DisplayName("Build")
    inner class Build {
        @Test
        @DisplayName("Should build NewsSentiment instance")
        fun build_BuildsInstance() { }
    }
}
