package org.cryptotrader.data.library.communication.request;

import lombok.Data;

@Data
public class NewsSentimentRequest {
    private Long articleId;
    private String title;
    private String publishDate;
    private String source;
    private String url;
    private double positiveScore;
    private double neutralScore;
    private double negativeScore;
    private double compositeScore;
    private double cryptoRelevance;
    private String lastUpdated;
    public NewsSentimentRequest(final Long articleId,
                                final String title,
                                final String publishDate,
                                final String source,
                                final String url,
                                final double positiveScore,
                                final double neutralScore,
                                final double negativeScore,
                                final double compositeScore,
                                final double cryptoRelevance,
                                final String lastUpdated) {
        this.articleId = articleId;
        this.title = title;
        this.publishDate = publishDate;
        this.source = source;
        this.url = url;
        this.positiveScore = positiveScore;
        this.neutralScore = neutralScore;
        this.negativeScore = negativeScore;
        this.compositeScore = compositeScore;
        this.cryptoRelevance = cryptoRelevance;
        this.lastUpdated = lastUpdated;
    }
}
