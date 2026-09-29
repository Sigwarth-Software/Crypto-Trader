package org.cryptotrader.data.library.entity.training.builder;

import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.data.library.entity.prediction.ModelType;
import org.cryptotrader.data.library.entity.prediction.PricePrediction;
import org.cryptotrader.data.library.entity.prediction.PricePredictionLookup;
import org.cryptotrader.data.library.entity.training.TrainingSession;
import org.cryptotrader.data.library.entity.training.builder.models.AbstractTrainingSession;
import org.cryptotrader.data.library.entity.training.specs.QueryLoad;
import org.cryptotrader.data.library.entity.training.specs.TrainingDevice;
import org.cryptotrader.data.library.entity.training.specs.TrainingQueryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

public class TrainingSessionBuilder extends AbstractTrainingSession {
    private @Nullable Currency currency;
    private @Nullable PricePrediction prediction;
    private int numRows;
    private int epochsTrained;
    private int maxEpochs;
    private double startingLoss;
    private double finalLoss;
    private @Nullable ModelType modelType;
    private @Nullable TrainingQueryType queryType;
    private @Nullable LocalDateTime trainingStartTime;
    private @Nullable LocalDateTime trainingEndTime;
    private @Nullable LocalDateTime queryStartTime;
    private @Nullable LocalDateTime queryEndTime;
    private int sequenceLength;
    private int batchSize;
    private int dimensionWidth;
    private @Nullable QueryLoad queryLoad;
    private @Nullable Integer queryBatchSize;
    private @Nullable TrainingDevice trainingDevice;
    private @Nullable Integer shortSequenceLength;
    private @Nullable Integer mediumSequenceLength;
    private @Nullable Integer longSequenceLength;


    private final @NotNull PricePredictionLookup pricePredictionLookup;

    public TrainingSessionBuilder(
        @NotNull final PricePredictionLookup pricePredictionLookup
    ) {
        this.currency = null;
        this.prediction = null;
        this.numRows = 0;
        this.epochsTrained = 0;
        this.maxEpochs = 0;
        this.startingLoss = 0.0;
        this.finalLoss = 0.0;
        this.modelType = null;
        this.queryType = null;
        this.trainingStartTime = null;
        this.trainingEndTime = null;
        this.queryStartTime = null;
        this.queryEndTime = null;
        this.sequenceLength = 0;
        this.batchSize = 0;
        this.dimensionWidth = 0;
        this.queryLoad = null;
        this.queryBatchSize = null;
        this.trainingDevice = null;
        this.shortSequenceLength = null;
        this.mediumSequenceLength = null;
        this.longSequenceLength = null;
        this.pricePredictionLookup = pricePredictionLookup;
    }

    @Override
    public @NotNull AbstractTrainingSession currency(final Currency currency) {
        this.currency = currency;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession currency(final String currencyCode) {
        this.currency = Currency.fromExisting(currencyCode);
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession prediction(final PricePrediction prediction) {
        this.prediction = prediction;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession prediction(final @NotNull Long predictionId) {
        this.prediction = this.pricePredictionLookup.getById(predictionId);
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession numRows(final int numRows) {
        this.numRows = numRows;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession epochsTrained(final int epochsTrained) {
        this.epochsTrained = epochsTrained;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession maxEpochs(final int maxEpochs) {
        this.maxEpochs = maxEpochs;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession startingLoss(final double startingLoss) {
        this.startingLoss = startingLoss;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession finalLoss(final double finalLoss) {
        this.finalLoss = finalLoss;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession modelType(final @NotNull ModelType modelType) {
        this.modelType = modelType;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession modelType(final @NotNull String modelType) {
        this.modelType = ModelType.from(modelType);
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession queryType(final @NotNull TrainingQueryType queryType) {
        this.queryType = queryType;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession queryType(final @NotNull String queryType) {
        this.queryType = TrainingQueryType.from(queryType);
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession trainingStartTime(final LocalDateTime startTime) {
        this.trainingStartTime = startTime;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession trainingStartTime(final @NotNull String startTime) {
        this.trainingStartTime = LocalDateTime.parse(startTime);
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession trainingEndTime(final LocalDateTime endTime) {
        this.trainingEndTime = endTime;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession trainingEndTime(final @NotNull String endTime) {
        this.trainingEndTime = LocalDateTime.parse(endTime);
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession queryStartTime(final LocalDateTime startTime) {
        this.queryStartTime = startTime;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession queryStartTime(final @NotNull String startTime) {
        this.queryStartTime = LocalDateTime.parse(startTime);
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession queryEndTime(final LocalDateTime endTime) {
        this.queryEndTime = endTime;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession queryEndTime(final @NotNull String endTime) {
        this.queryEndTime = LocalDateTime.parse(endTime);
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession sequenceLength(final int sequenceLength) {
        this.sequenceLength = sequenceLength;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession batchSize(final int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession dimensionWidth(final int dimensionWidth) {
        this.dimensionWidth = dimensionWidth;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession queryLoad(final @NotNull QueryLoad queryLoad) {
        this.queryLoad = queryLoad;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession queryLoad(final @NotNull String queryLoad) {
        this.queryLoad = QueryLoad.from(queryLoad);
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession queryBatchSize(final Integer queryBatchSize) {
        this.queryBatchSize = queryBatchSize;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession trainingDevice(
        final @NotNull TrainingDevice trainingDevice
    ) {
        this.trainingDevice = trainingDevice;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession trainingDevice(final @NotNull String trainingDevice) {
        this.trainingDevice = TrainingDevice.from(trainingDevice);
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession shortSequenceLength(final Integer shortSequenceLength) {
        this.shortSequenceLength = shortSequenceLength;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession mediumSequenceLength(final Integer mediumSequenceLength) {
        this.mediumSequenceLength = mediumSequenceLength;
        return this;
    }

    @Override
    public @NotNull AbstractTrainingSession longSequenceLength(final Integer longSequenceLength) {
        this.longSequenceLength = longSequenceLength;
        return this;
    }

    @Override
    public @NotNull TrainingSession build() {
        return new TrainingSession(this.currency,
                                   this.prediction,
                                   this.numRows,
                                   this.epochsTrained,
                                   this.maxEpochs,
                                   this.startingLoss,
                                   this.finalLoss,
                                   this.modelType,
                                   this.queryType,
                                   this.trainingStartTime,
                                   this.trainingEndTime,
                                   this.queryStartTime,
                                   this.queryEndTime,
                                   this.sequenceLength,
                                   this.batchSize,
                                   this.dimensionWidth,
                                   this.queryLoad,
                                   this.queryBatchSize,
                                   this.trainingDevice,
                                   this.shortSequenceLength,
                                   this.mediumSequenceLength,
                                   this.longSequenceLength
        );
    }
}
