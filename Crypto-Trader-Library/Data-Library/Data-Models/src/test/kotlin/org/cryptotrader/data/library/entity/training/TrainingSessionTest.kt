package org.cryptotrader.data.library.entity.training

import org.cryptotrader.data.library.entity.prediction.PricePredictionLookup
import org.cryptotrader.data.library.entity.training.TrainingSession
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mock

@Tag("TrainingSession")
@Tag("entity")
@DisplayName("Training Session Entity")
class TrainingSessionTest : CryptoTraderTest() {

    @Mock
    lateinit var lookup: PricePredictionLookup

    private lateinit var trainingSession: TrainingSession

    @BeforeEach
    fun setUp() {
        this.trainingSession = TrainingSession()
    }

    @Nested
    @Tag("builder")
    @DisplayName("Builder")
    inner class BuilderMethod {
        @Test
        @DisplayName("Deprecated builder() should throw or be discouraged")
        fun builder_Deprecated() { }

        @Test
        @DisplayName("Should return builder with PricePredictionLookup")
        fun builder_WithLookup_ReturnsBuilder() { }
    }
}
