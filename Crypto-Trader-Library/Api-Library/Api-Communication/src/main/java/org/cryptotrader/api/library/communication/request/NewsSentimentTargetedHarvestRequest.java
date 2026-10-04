package org.cryptotrader.api.library.communication.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NewsSentimentTargetedHarvestRequest {
    private int numArticles;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean includeForbes;
    public NewsSentimentTargetedHarvestRequest(final int numArticles,
                                               final LocalDate startDate,
                                               final LocalDate endDate,
                                               final boolean includeForbes) {
        this.numArticles = numArticles;
        this.startDate = startDate;
        this.endDate = endDate;
        this.includeForbes = includeForbes;
    }
}
