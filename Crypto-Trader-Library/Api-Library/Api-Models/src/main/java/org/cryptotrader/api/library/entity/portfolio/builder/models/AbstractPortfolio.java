package org.cryptotrader.api.library.entity.portfolio.builder.models;

import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;
import org.cryptotrader.api.library.entity.portfolio.PortfolioHistory;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.universal.library.model.BuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

/** The class defining a builder for portfolio entities. */
public abstract class AbstractPortfolio implements BuilderFactory<Portfolio> {
    public abstract @NotNull AbstractPortfolio user(@Nullable ProductUser user);

    public abstract @NotNull AbstractPortfolio dollarBalance(double dollarBalance);

    public abstract @NotNull AbstractPortfolio shareBalance(double shareBalance);

    public abstract @NotNull AbstractPortfolio totalWorth(double totalWorth);

    public abstract @NotNull AbstractPortfolio lastUpdated(@Nullable LocalDateTime lastUpdated);

    public abstract @NotNull AbstractPortfolio assets(@NotNull List<PortfolioAsset> assets);

    public abstract @NotNull AbstractPortfolio portfolioHistory(
        @NotNull List<PortfolioHistory> portfolioHistory
    );
}
