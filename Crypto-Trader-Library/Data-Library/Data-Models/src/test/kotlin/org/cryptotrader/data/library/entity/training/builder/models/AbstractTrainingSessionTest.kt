package org.cryptotrader.data.library.entity.training.builder.models

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("AbstractTrainingSession")
@Tag("entity")
@DisplayName("Abstract Training Session Builder Model")
class AbstractTrainingSessionTest : CryptoTraderTest() {

    @Nested
    @Tag("fluentSetters")
    @DisplayName("Fluent Setters")
    inner class FluentSetters {
        @Test
        @DisplayName("Should support currency setters")
        fun currency_Setters() { }

        @Test
        @DisplayName("Should support prediction setters")
        fun prediction_Setters() { }

        @Test
        @DisplayName("Should support training parameter setters")
        fun trainingParameters_Setters() { }

        @Test
        @DisplayName("Should support timing setters")
        fun time_Setters() { }

        @Test
        @DisplayName("Should support dimension and batch setters")
        fun dimensionBatch_Setters() { }

        @Test
        @DisplayName("Should support query setters")
        fun query_Setters() { }

        @Test
        @DisplayName("Should support device and sequence length setters")
        fun deviceAndSequence_Setters() { }
    }

    @Nested
    @Tag("build")
    @DisplayName("Build")
    inner class Build {
        @Test
        @DisplayName("Should build TrainingSession instance")
        fun build_BuildsInstance() { }
    }
}
