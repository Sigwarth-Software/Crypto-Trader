package org.cryptotrader.data.library.entity.currency.builder.models

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("AbstractCurrency")
@Tag("entity")
@DisplayName("Abstract Currency Builder Model")
class AbstractCurrencyTest : CryptoTraderTest() {

    @Nested
    @Tag("fluentSetters")
    @DisplayName("Fluent Setters")
    inner class FluentSetters {
        @Test
        @DisplayName("Should support name setter")
        fun name_Setter() { }

        @Test
        @DisplayName("Should support currencyCode setter")
        fun currencyCode_Setter() { }

        @Test
        @DisplayName("Should support urlPath setter")
        fun urlPath_Setter() { }

        @Test
        @DisplayName("Should support value setter")
        fun value_Setter() { }

        @Test
        @DisplayName("Should support lastUpdated setter")
        fun lastUpdated_Setter() { }
    }

    @Nested
    @Tag("build")
    @DisplayName("Build")
    inner class Build {
        @Test
        @DisplayName("Should build Currency instance")
        fun build_BuildsInstance() { }
    }
}
