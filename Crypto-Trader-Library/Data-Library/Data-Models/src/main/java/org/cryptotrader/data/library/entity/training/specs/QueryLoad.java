package org.cryptotrader.data.library.entity.training.specs;

public enum QueryLoad {
    BATCHES("batches"),
    BULK("bulk");
    public final String load;

    QueryLoad(final String load) {
        this.load = load;
    }

    public static QueryLoad from(final String load) {
        return switch (load) {
            case "batches" -> QueryLoad.BATCHES;
            case "bulk" -> QueryLoad.BULK;
            default -> throw new IllegalArgumentException("Unknown query load: " + load);
        };
    }
}
