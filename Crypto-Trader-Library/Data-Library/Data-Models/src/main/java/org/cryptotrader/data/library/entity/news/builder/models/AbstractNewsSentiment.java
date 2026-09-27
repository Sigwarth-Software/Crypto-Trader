package org.cryptotrader.data.library.entity.news.builder.models;

import org.cryptotrader.data.library.entity.news.NewsSentiment;
import org.cryptotrader.universal.library.model.BuilderFactory;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public abstract class AbstractNewsSentiment implements BuilderFactory<NewsSentiment> {
    public abstract @NotNull AbstractNewsSentiment articleId(Long articleId);
    public abstract @NotNull AbstractNewsSentiment title(String title);
    public abstract @NotNull AbstractNewsSentiment publishedDate(String publishedDate);
    public abstract @NotNull AbstractNewsSentiment publishedDate(LocalDateTime publishedDate);
    public abstract @NotNull AbstractNewsSentiment source(String source);
    public abstract @NotNull AbstractNewsSentiment url(String url);
    public abstract @NotNull AbstractNewsSentiment positiveScore(double positiveScore);
    public abstract @NotNull AbstractNewsSentiment neutralScore(double neutralScore);
    public abstract @NotNull AbstractNewsSentiment negativeScore(double negativeScore);
    public abstract @NotNull AbstractNewsSentiment compositeScore(double compositeScore);
    public abstract @NotNull AbstractNewsSentiment cryptoRelevance(double cryptoRelevance);
    public abstract @NotNull AbstractNewsSentiment lastUpdated(LocalDateTime lastUpdated);
    public abstract @NotNull AbstractNewsSentiment lastUpdated(String lastUpdated);
}