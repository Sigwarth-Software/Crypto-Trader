package org.cryptotrader.data.library.entity.training.builder.models;

import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.data.library.entity.prediction.ModelType;
import org.cryptotrader.data.library.entity.prediction.PricePrediction;
import org.cryptotrader.data.library.entity.training.TrainingSession;
import org.cryptotrader.data.library.entity.training.specs.QueryLoad;
import org.cryptotrader.data.library.entity.training.specs.TrainingDevice;
import org.cryptotrader.data.library.entity.training.specs.TrainingQueryType;
import org.cryptotrader.universal.library.model.BuilderFactory;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/** Class definition for training session entity builder factory. */
public abstract class AbstractTrainingSession implements BuilderFactory<TrainingSession> {
    public abstract @NotNull AbstractTrainingSession currency(final Currency currency);

    public abstract @NotNull AbstractTrainingSession currency(final String currencyCode);

    public abstract @NotNull AbstractTrainingSession prediction(final PricePrediction prediction);

    public abstract @NotNull AbstractTrainingSession prediction(final Long predictionId);

    public abstract @NotNull AbstractTrainingSession numRows(final int numRows);

    public abstract @NotNull AbstractTrainingSession epochsTrained(final int epochsTrained);

    public abstract @NotNull AbstractTrainingSession maxEpochs(final int maxEpochs);

    public abstract @NotNull AbstractTrainingSession startingLoss(final double startingLoss);

    public abstract @NotNull AbstractTrainingSession finalLoss(final double finalLoss);

    public abstract @NotNull AbstractTrainingSession modelType(final ModelType modelType);

    public abstract @NotNull AbstractTrainingSession modelType(final String modelType);

    public abstract @NotNull AbstractTrainingSession queryType(final TrainingQueryType queryType);
    public abstract @NotNull AbstractTrainingSession queryType(final String queryType);

    public abstract @NotNull AbstractTrainingSession trainingStartTime(
        final LocalDateTime startTime
    );

    public abstract @NotNull AbstractTrainingSession trainingStartTime(final String startTime);

    public abstract @NotNull AbstractTrainingSession trainingEndTime(final LocalDateTime endTime);

    public abstract @NotNull AbstractTrainingSession trainingEndTime(final String endTime);

    public abstract @NotNull AbstractTrainingSession queryStartTime(final LocalDateTime startTime);

    public abstract @NotNull AbstractTrainingSession queryStartTime(final String startTime);

    public abstract @NotNull AbstractTrainingSession queryEndTime(final LocalDateTime endTime);

    public abstract @NotNull AbstractTrainingSession queryEndTime(final String endTime);

    public abstract @NotNull AbstractTrainingSession sequenceLength(final int sequenceLength);

    public abstract @NotNull AbstractTrainingSession batchSize(final int batchSize);

    public abstract @NotNull AbstractTrainingSession dimensionWidth(final int dimensionWidth);

    public abstract @NotNull AbstractTrainingSession queryLoad(final QueryLoad queryLoad);

    public abstract @NotNull AbstractTrainingSession queryLoad(final String queryLoad);

    public abstract @NotNull AbstractTrainingSession queryBatchSize(
        final Integer queryBatchSize
    );

    public abstract @NotNull AbstractTrainingSession trainingDevice(
        final TrainingDevice trainingDevice
    );

    public abstract @NotNull AbstractTrainingSession trainingDevice(
        final String trainingDevice
    );

    public abstract @NotNull AbstractTrainingSession shortSequenceLength(
        final Integer shortSequenceLength
    );
    public abstract @NotNull AbstractTrainingSession mediumSequenceLength(
        final Integer mediumSequenceLength
    );

    public abstract @NotNull AbstractTrainingSession longSequenceLength(
        final Integer longSequenceLength
    );
}
