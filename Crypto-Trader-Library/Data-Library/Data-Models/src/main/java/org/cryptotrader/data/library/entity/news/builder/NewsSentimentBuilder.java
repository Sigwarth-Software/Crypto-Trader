package org.cryptotrader.data.library.entity.news.builder;

import org.cryptotrader.data.library.entity.news.NewsSentiment;
import org.cryptotrader.data.library.entity.news.builder.models.AbstractNewsSentiment;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** A builder factory for creating news sentiment entities. */
public class NewsSentimentBuilder extends AbstractNewsSentiment {
    private Long articleId;
    private String title;
    private LocalDateTime publishedDate;
    private String source;
    private String url;
    private double positiveScore;
    private double neutralScore;
    private double negativeScore;
    private double compositeScore;
    private double cryptoRelevance;
    private LocalDateTime lastUpdated;

    public NewsSentimentBuilder() {
        this.articleId = 0L;
        this.title = "";
        this.publishedDate = null;
        this.source = "";
        this.url = "";
        this.positiveScore = 0;
        this.neutralScore = 0;
        this.negativeScore = 0;
        this.compositeScore = 0;
        this.cryptoRelevance = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public @NotNull AbstractNewsSentiment articleId(final Long articleId) {
        this.articleId = articleId;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment title(final String title) {
        this.title = title;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment publishedDate(final String publishedDate) {
        this.publishedDate = LocalDateTime.parse(publishedDate,
                                                 DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment publishedDate(final LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment source(final String source) {
        this.source = source;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment url(final String url) {
        this.url = url;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment positiveScore(final double positiveScore) {
        this.positiveScore = positiveScore;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment neutralScore(final double neutralScore) {
        this.neutralScore = neutralScore;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment negativeScore(final double negativeScore) {
        this.negativeScore = negativeScore;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment compositeScore(final double compositeScore) {
        this.compositeScore = compositeScore;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment cryptoRelevance(final double cryptoRelevance) {
        this.cryptoRelevance = cryptoRelevance;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment lastUpdated(final LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    @Override
    public @NotNull AbstractNewsSentiment lastUpdated(final String lastUpdated) {
        this.lastUpdated = LocalDateTime.parse(lastUpdated);
        return this;
    }

    @Override
    public @NotNull NewsSentiment build() {
        return new NewsSentiment(
                this.articleId,
                this.title,
                this.publishedDate,
                this.source,
                this.url,
                this.positiveScore,
                this.neutralScore,
                this.negativeScore,
                this.compositeScore,
                this.cryptoRelevance,
                this.lastUpdated
        );
    }
}
