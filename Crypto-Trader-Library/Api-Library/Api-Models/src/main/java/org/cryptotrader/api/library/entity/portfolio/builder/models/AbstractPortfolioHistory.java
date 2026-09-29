package org.cryptotrader.api.library.entity.portfolio.builder.models;

import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.portfolio.PortfolioHistory;
import org.cryptotrader.universal.library.model.BuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/** A class defining a builder for portfolio history entities. */
public abstract class AbstractPortfolioHistory implements BuilderFactory<PortfolioHistory> {
    public abstract @NotNull AbstractPortfolioHistory portfolio(@NotNull Portfolio portfolio);

    public abstract @NotNull AbstractPortfolioHistory dollarBalance(double dollarBalance);

    public abstract @NotNull AbstractPortfolioHistory shareBalance(double shareBalance);

    public abstract @NotNull AbstractPortfolioHistory totalWorth(double totalWorth);

    public abstract @NotNull AbstractPortfolioHistory valueChange(double valueChange);

    public abstract @NotNull AbstractPortfolioHistory tradeOccurred(boolean tradeOccurred);

    public abstract @NotNull AbstractPortfolioHistory lastUpdated(
        @Nullable LocalDateTime lastUpdated
    );
}
