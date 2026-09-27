package org.cryptotrader.data.library.entity.prediction.builder.models;

import org.cryptotrader.data.library.entity.prediction.ModelType;
import org.cryptotrader.data.library.entity.prediction.PricePrediction;
import org.cryptotrader.universal.library.model.BuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/** The class definition for a price prediction builder. */
public abstract class AbstractPricePrediction implements BuilderFactory<PricePrediction> {
    public abstract @NotNull AbstractPricePrediction currencyCode(@Nullable String currencyCode);

    public abstract @NotNull AbstractPricePrediction currencyName(@Nullable String currencyName);

    public abstract @NotNull AbstractPricePrediction predictedPrice(double predictedPrice);

    public abstract @NotNull AbstractPricePrediction actualPrice(double actualPrice);

    public abstract @NotNull AbstractPricePrediction priceDifference(double priceDifference);

    public abstract @NotNull AbstractPricePrediction percentDifference(double percentDifference);

    public abstract @NotNull AbstractPricePrediction lastUpdated(
        @Nullable LocalDateTime localDateTime
    );

    public abstract @NotNull AbstractPricePrediction numRows(int numRows);

    public abstract @NotNull AbstractPricePrediction modelType(@NotNull String modelType);

    public abstract @NotNull AbstractPricePrediction modelType(@NotNull ModelType modelType);
}
