package org.cryptotrader.data.library.entity.prediction;

import org.jetbrains.annotations.NotNull;

public enum ModelType {
    LSTM("lstm"),
    COMPLEX_LSTM("complex_lstm"),
    MULTI_LAYER("multi_layer"),
    COMPLEX_MULTI_LAYER("complex_multi_layer");
    public final String modelType;
    ModelType(final String modelType) {
        this.modelType = modelType;
    }
    public static @NotNull ModelType from(final @NotNull String modelType) {
        return switch (modelType) {
            case "lstm" -> ModelType.LSTM;
            case "complex_lstm" -> ModelType.COMPLEX_LSTM;
            case "multi_layer" -> ModelType.MULTI_LAYER;
            case "complex_multi_layer" -> ModelType.COMPLEX_MULTI_LAYER;
            default -> throw new IllegalArgumentException("Unknown model type: " + modelType);
        };
    }
}
