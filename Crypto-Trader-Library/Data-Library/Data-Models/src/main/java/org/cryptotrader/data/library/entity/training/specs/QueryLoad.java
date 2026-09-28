package org.cryptotrader.data.library.entity.training.specs;

import org.jetbrains.annotations.NotNull;

public enum QueryLoad {
    BATCHES("batches"),
    BULK("bulk");
    public final String load;

    QueryLoad(final String load) {
        this.load = load;
    }

    public static @NotNull QueryLoad from(final @NotNull String load) {
        return switch (load) {
            case "batches" -> QueryLoad.BATCHES;
            case "bulk" -> QueryLoad.BULK;
            default -> throw new IllegalArgumentException("Unknown query load: " + load);
        };
    }
}
