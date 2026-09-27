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

import java.time.LocalDateTime;

public class TrainingSessionBuilder extends AbstractTrainingSession {
    private Currency currency;
    private PricePrediction prediction;
    private int numRows;
    private int epochsTrained;
    private int maxEpochs;
    private double startingLoss;
    private double finalLoss;
    private ModelType modelType;
    private TrainingQueryType queryType;
    private LocalDateTime trainingStartTime;
    private LocalDateTime trainingEndTime;
    private LocalDateTime queryStartTime;
    private LocalDateTime queryEndTime;
    private int sequenceLength;
    private int batchSize;
    private int dimensionWidth;
    private QueryLoad queryLoad;
    private Integer queryBatchSize;
    private TrainingDevice trainingDevice;
    private Integer shortSequenceLength;
    private Integer mediumSequenceLength;
    private Integer longSequenceLength;


    private final PricePredictionLookup pricePredictionLookup;

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
    public AbstractTrainingSession currency(final Currency currency) {
        this.currency = currency;
        return this;
    }

    @Override
    public AbstractTrainingSession currency(final String currencyCode) {
        this.currency = Currency.fromExisting(currencyCode);
        return this;
    }

    @Override
    public AbstractTrainingSession prediction(final PricePrediction prediction) {
        this.prediction = prediction;
        return this;
    }

    @Override
    public AbstractTrainingSession prediction(final Long predictionId) {
        this.prediction = this.pricePredictionLookup.getById(predictionId);
        return this;
    }

    @Override
    public AbstractTrainingSession numRows(final int numRows) {
        this.numRows = numRows;
        return this;
    }

    @Override
    public AbstractTrainingSession epochsTrained(final int epochsTrained) {
        this.epochsTrained = epochsTrained;
        return this;
    }

    @Override
    public AbstractTrainingSession maxEpochs(final int maxEpochs) {
        this.maxEpochs = maxEpochs;
        return this;
    }

    @Override
    public AbstractTrainingSession startingLoss(final double startingLoss) {
        this.startingLoss = startingLoss;
        return this;
    }

    @Override
    public AbstractTrainingSession finalLoss(final double finalLoss) {
        this.finalLoss = finalLoss;
        return this;
    }

    @Override
    public AbstractTrainingSession modelType(final ModelType modelType) {
        this.modelType = modelType;
        return this;
    }

    @Override
    public AbstractTrainingSession modelType(final String modelType) {
        this.modelType = ModelType.from(modelType);
        return this;
    }

    @Override
    public AbstractTrainingSession queryType(final TrainingQueryType queryType) {
        this.queryType = queryType;
        return this;
    }

    @Override
    public AbstractTrainingSession queryType(final String queryType) {
        this.queryType = TrainingQueryType.from(queryType);
        return this;
    }

    @Override
    public AbstractTrainingSession trainingStartTime(final LocalDateTime startTime) {
        this.trainingStartTime = startTime;
        return this;
    }

    @Override
    public AbstractTrainingSession trainingStartTime(final String startTime) {
        this.trainingStartTime = LocalDateTime.parse(startTime);
        return this;
    }

    @Override
    public AbstractTrainingSession trainingEndTime(final LocalDateTime endTime) {
        this.trainingEndTime = endTime;
        return this;
    }

    @Override
    public AbstractTrainingSession trainingEndTime(final String endTime) {
        this.trainingEndTime = LocalDateTime.parse(endTime);
        return this;
    }

    @Override
    public AbstractTrainingSession queryStartTime(final LocalDateTime startTime) {
        this.queryStartTime = startTime;
        return this;
    }

    @Override
    public AbstractTrainingSession queryStartTime(final String startTime) {
        this.queryStartTime = LocalDateTime.parse(startTime);
        return this;
    }

    @Override
    public AbstractTrainingSession queryEndTime(final LocalDateTime endTime) {
        this.queryEndTime = endTime;
        return this;
    }

    @Override
    public AbstractTrainingSession queryEndTime(final String endTime) {
        this.queryEndTime = LocalDateTime.parse(endTime);
        return this;
    }

    @Override
    public AbstractTrainingSession sequenceLength(final int sequenceLength) {
        this.sequenceLength = sequenceLength;
        return this;
    }

    @Override
    public AbstractTrainingSession batchSize(final int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Override
    public AbstractTrainingSession dimensionWidth(final int dimensionWidth) {
        this.dimensionWidth = dimensionWidth;
        return this;
    }

    @Override
    public AbstractTrainingSession queryLoad(final QueryLoad queryLoad) {
        this.queryLoad = queryLoad;
        return this;
    }

    @Override
    public AbstractTrainingSession queryLoad(final String queryLoad) {
        this.queryLoad = QueryLoad.from(queryLoad);
        return this;
    }

    @Override
    public AbstractTrainingSession queryBatchSize(final Integer queryBatchSize) {
        this.queryBatchSize = queryBatchSize;
        return this;
    }

    @Override
    public AbstractTrainingSession trainingDevice(final TrainingDevice trainingDevice) {
        this.trainingDevice = trainingDevice;
        return this;
    }

    @Override
    public AbstractTrainingSession trainingDevice(final String trainingDevice) {
        this.trainingDevice = TrainingDevice.from(trainingDevice);
        return this;
    }

    @Override
    public AbstractTrainingSession shortSequenceLength(final Integer shortSequenceLength) {
        this.shortSequenceLength = shortSequenceLength;
        return this;
    }

    @Override
    public AbstractTrainingSession mediumSequenceLength(final Integer mediumSequenceLength) {
        this.mediumSequenceLength = mediumSequenceLength;
        return this;
    }

    @Override
    public AbstractTrainingSession longSequenceLength(final Integer longSequenceLength) {
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
