package org.cryptotrader.data.library.entity.training.specs;

public enum TrainingQueryType {
    CURRENT_PRICE("current_price"),
    HISTORICAL_PRICE("historical_price"),
    HISTORICAL_PRICE_SPACED("historical_price_spaced");
    public final String type;

    TrainingQueryType(final String type) {
        this.type = type;
    }

    public static TrainingQueryType from(final String type) {
        return switch (type) {
            case "current_price" -> TrainingQueryType.CURRENT_PRICE;
            case "historical_price" -> TrainingQueryType.HISTORICAL_PRICE;
            case "historical_price_spaced" -> TrainingQueryType.HISTORICAL_PRICE_SPACED;
            default -> throw new IllegalArgumentException("Unknown query type: " + type);
        };
    }
}