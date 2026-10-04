package org.cryptotrader.api.library.communication.request;

import lombok.Data;

@Data
public class NewsSentimentHarvestRequest {
    private int numArticles;
    private int daysOffset;
    private int numDays;
    private boolean includeForbes;
    public NewsSentimentHarvestRequest(final int numArticles,
                                       final int daysOffset,
                                       final int numDays,
                                       final boolean includeForbes) {
        this.numArticles = numArticles;
        this.daysOffset = daysOffset;
        this.numDays = numDays;
        this.includeForbes = includeForbes;
    }
}
