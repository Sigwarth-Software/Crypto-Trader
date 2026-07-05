package org.cryptotrader.data.library.entity.training.builder

import org.cryptotrader.data.library.entity.prediction.PricePredictionLookup
import org.cryptotrader.data.library.entity.training.builder.TrainingSessionBuilder
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mock

@Tag("TrainingSessionBuilder")
@Tag("entity")
@DisplayName("Training Session Builder")
class TrainingSessionBuilderTest : CryptoTraderTest() {

    @Mock
    lateinit var lookup: PricePredictionLookup

    private lateinit var builder: TrainingSessionBuilder

    @BeforeEach
    fun setUp() {
        this.builder = TrainingSessionBuilder(lookup)
    }

    @Nested
    @Tag("currency")
    @DisplayName("Currency")
    inner class CurrencySetter {
        @Test
        @DisplayName("Should set currency by object")
        fun currency_ByObject_Sets() { }

        @Test
        @DisplayName("Should set currency by code")
        fun currency_ByCode_Sets() { }
    }

    @Nested
    @Tag("prediction")
    @DisplayName("Prediction")
    inner class PredictionSetter {
        @Test
        @DisplayName("Should set prediction by object")
        fun prediction_ByObject_Sets() { }

        @Test
        @DisplayName("Should set prediction by id")
        fun prediction_ById_Sets() { }
    }

    @Nested
    @Tag("trainingParams")
    @DisplayName("Training Params")
    inner class TrainingParams {
        @Test
        @DisplayName("Should set numRows")
        fun numRows_Sets() { }

        @Test
        @DisplayName("Should set epochsTrained")
        fun epochsTrained_Sets() { }

        @Test
        @DisplayName("Should set maxEpochs")
        fun maxEpochs_Sets() { }

        @Test
        @DisplayName("Should set startingLoss")
        fun startingLoss_Sets() { }

        @Test
        @DisplayName("Should set finalLoss")
        fun finalLoss_Sets() { }

        @Test
        @DisplayName("Should set modelType by enum")
        fun modelType_ByEnum_Sets() { }

        @Test
        @DisplayName("Should set modelType by string")
        fun modelType_ByString_Sets() { }

        @Test
        @DisplayName("Should set queryType by enum")
        fun queryType_ByEnum_Sets() { }

        @Test
        @DisplayName("Should set queryType by string")
        fun queryType_ByString_Sets() { }

        @Test
        @DisplayName("Should set training start/end times")
        fun trainingTimes_Setters() { }

        @Test
        @DisplayName("Should set query start/end times")
        fun queryTimes_Setters() { }

        @Test
        @DisplayName("Should set sequenceLength, batchSize, dimensionWidth")
        fun dimensionsAndBatch_Setters() { }

        @Test
        @DisplayName("Should set queryLoad by enum")
        fun queryLoad_ByEnum_Sets() { }

        @Test
        @DisplayName("Should set queryLoad by string")
        fun queryLoad_ByString_Sets() { }

        @Test
        @DisplayName("Should set queryBatchSize")
        fun queryBatchSize_Sets() { }

        @Test
        @DisplayName("Should set trainingDevice by enum")
        fun trainingDevice_ByEnum_Sets() { }

        @Test
        @DisplayName("Should set trainingDevice by string")
        fun trainingDevice_ByString_Sets() { }

        @Test
        @DisplayName("Should set short/medium/long sequence lengths")
        fun sequenceLengths_Setters() { }
    }

    @Nested
    @Tag("build")
    @DisplayName("Build")
    inner class Build {
        @Test
        @DisplayName("Should build TrainingSession from set fields")
        fun build_Builds() { }
    }
}
