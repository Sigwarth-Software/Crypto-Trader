package org.cryptotrader.data.library.entity.news.builder.models;

import org.cryptotrader.data.library.entity.news.NewsSentiment;
import org.cryptotrader.universal.library.model.BuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/** Class definition for a new sentiment builder. */
public abstract class AbstractNewsSentiment implements BuilderFactory<NewsSentiment> {

    public abstract @NotNull AbstractNewsSentiment articleId(@Nullable final Long articleId);

    public abstract @NotNull AbstractNewsSentiment title(@Nullable final String title);

    public abstract @NotNull AbstractNewsSentiment publishedDate(
        @Nullable final String publishedDate
    );

    public abstract @NotNull AbstractNewsSentiment publishedDate(
        @Nullable final LocalDateTime publishedDate
    );

    public abstract @NotNull AbstractNewsSentiment source(@Nullable final String source);

    public abstract @NotNull AbstractNewsSentiment url(@Nullable final String url);

    public abstract @NotNull AbstractNewsSentiment positiveScore(double positiveScore);

    public abstract @NotNull AbstractNewsSentiment neutralScore(double neutralScore);

    public abstract @NotNull AbstractNewsSentiment negativeScore(double negativeScore);

    public abstract @NotNull AbstractNewsSentiment compositeScore(double compositeScore);

    public abstract @NotNull AbstractNewsSentiment cryptoRelevance(double cryptoRelevance);

    public abstract @NotNull AbstractNewsSentiment lastUpdated(
        @Nullable final LocalDateTime lastUpdated
    );

    public abstract @NotNull AbstractNewsSentiment lastUpdated(@Nullable final String lastUpdated);
}
