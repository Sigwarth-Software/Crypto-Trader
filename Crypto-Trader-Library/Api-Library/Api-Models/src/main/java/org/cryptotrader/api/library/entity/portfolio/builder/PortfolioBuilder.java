package org.cryptotrader.api.library.entity.portfolio.builder;

import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;
import org.cryptotrader.api.library.entity.portfolio.PortfolioHistory;
import org.cryptotrader.api.library.entity.portfolio.builder.models.AbstractPortfolio;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** A builder factory for portfolio entities. */
public class PortfolioBuilder extends AbstractPortfolio {
    private ProductUser user;
    private double dollarBalance;
    private double shareBalance;
    private double totalWorth;
    private LocalDateTime lastUpdated;
    private List<PortfolioAsset> assets;
    private List<PortfolioHistory> portfolioHistory;

    public PortfolioBuilder() {
        this.dollarBalance = 0;
        this.shareBalance = 0;
        this.totalWorth = 0;
        this.user = new ProductUser();
        this.assets = new ArrayList<>();
        this.portfolioHistory = new ArrayList<>();
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public @NotNull AbstractPortfolio user(@Nullable final ProductUser user) {
        this.user = user;
        return this;
    }

    @Override
    public @NotNull AbstractPortfolio dollarBalance(final double dollarBalance) {
        this.dollarBalance = dollarBalance;
        return this;
    }

    @Override
    public @NotNull AbstractPortfolio shareBalance(final double shareBalance) {
        this.shareBalance = shareBalance;
        return this;
    }

    @Override
    public @NotNull AbstractPortfolio totalWorth(final double totalWorth) {
        this.totalWorth = totalWorth;
        return this;
    }

    @Override
    public @NotNull AbstractPortfolio lastUpdated(@Nullable final LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    @Override
    public @NotNull AbstractPortfolio assets(@NotNull final List<PortfolioAsset> assets) {
        this.assets = assets;
        this.dollarBalance = Portfolio.getTotalDollarValue(assets);
        this.shareBalance = Portfolio.getTotalShareValue(assets);
        this.totalWorth = this.dollarBalance + this.shareBalance;
        this.lastUpdated = LocalDateTime.now();
        return this;
    }

    @Override
    public @NotNull AbstractPortfolio portfolioHistory(
        @NotNull final List<PortfolioHistory> portfolioHistory
    ) {
        this.portfolioHistory = portfolioHistory;
        return this;
    }

    @Override
    public @NotNull Portfolio build() {
        return new Portfolio(this.user,
                             this.dollarBalance,
                             this.shareBalance,
                             this.totalWorth,
                             this.lastUpdated,
                             this.assets,
                             this.portfolioHistory);
    }
}
