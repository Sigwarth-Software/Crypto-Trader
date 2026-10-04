package org.cryptotrader.data.library.entity.prediction.builder;

import org.cryptotrader.data.library.entity.prediction.ModelType;
import org.cryptotrader.data.library.entity.prediction.PricePrediction;
import org.cryptotrader.data.library.entity.prediction.builder.models.AbstractPricePrediction;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/** A builder factory for creating price prediction entities. */
public class PricePredictionBuilder extends AbstractPricePrediction {
    private String currencyCode;
    private String currencyName;
    private double predictedPrice;
    private double actualPrice;
    private double priceDifference;
    private double percentDifference;
    private LocalDateTime lastUpdated;
    private int numRows;
    private ModelType modelType;

    public PricePredictionBuilder() {
        this.currencyCode = "";
        this.currencyName = "";
        this.predictedPrice = 0.0;
        this.actualPrice = 0.0;
        this.percentDifference = 0.0;
        this.lastUpdated = LocalDateTime.now();
        this.numRows = 0;
        this.modelType = ModelType.LSTM;
    }

    @Override
    public @NotNull AbstractPricePrediction currencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    @Override
    public @NotNull AbstractPricePrediction currencyName(final String currencyName) {
        this.currencyName = currencyName;
        return this;
    }

    @Override
    public @NotNull AbstractPricePrediction predictedPrice(final double predictedPrice) {
        this.predictedPrice = predictedPrice;
        return this;
    }

    @Override
    public @NotNull AbstractPricePrediction actualPrice(final double actualPrice) {
        this.actualPrice = actualPrice;
        return this;
    }

    @Override
    public @NotNull AbstractPricePrediction priceDifference(final double priceDifference) {
        this.priceDifference = priceDifference;
        return this;
    }

    @Override
    public @NotNull AbstractPricePrediction percentDifference(final double percentDifference) {
        this.percentDifference = percentDifference;
        return this;
    }

    @Override
    public @NotNull AbstractPricePrediction lastUpdated(final LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    @Override
    public @NotNull AbstractPricePrediction numRows(final int numRows) {
        this.numRows = numRows;
        return this;
    }

    @Override
    public @NotNull AbstractPricePrediction modelType(@NotNull final String modelType) {
        this.modelType = ModelType.from(modelType);
        return this;
    }

    @Override
    public @NotNull AbstractPricePrediction modelType(@NotNull final ModelType modelType) {
        this.modelType = modelType;
        return this;
    }

    @Override
    public @NotNull PricePrediction build() {
        return new PricePrediction(this.currencyCode,
                                   this.currencyName,
                                   this.predictedPrice,
                                   this.actualPrice,
                                   this.priceDifference,
                                   this.percentDifference,
                                   this.modelType,
                                   this.numRows,
                                   this.lastUpdated);
    }
}
