package org.cryptotrader.api.library.model.trade;

import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;

public interface TradingEngine {
    boolean trade();
    boolean trade(double currentPrice);

    boolean canTrade();
    boolean canTrade(double currentPrice);

    // TODO: This should return an enum. Buy, sell, failure.
    void sell(double currentPrice);
    void buy(double currentPrice);
    PortfolioAsset getAsset();
}