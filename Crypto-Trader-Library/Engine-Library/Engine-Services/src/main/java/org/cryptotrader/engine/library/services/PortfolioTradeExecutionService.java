package org.cryptotrader.engine.library.services;

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAssetHistory;
import org.cryptotrader.api.library.entity.portfolio.PortfolioHistory;
import org.cryptotrader.api.library.entity.trade.TradeEvent;
import org.cryptotrader.api.library.entity.trade.TradeType;
import org.cryptotrader.api.library.model.trade.Trader;
import org.cryptotrader.api.library.model.trade.TradingEngine;
import org.cryptotrader.api.library.services.PortfolioService;
import org.cryptotrader.api.library.services.TradeEventService;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PortfolioTradeExecutionService {
    private final PortfolioService portfolioService;
    private final TradeEventService tradeEventService;

    @Autowired
    public PortfolioTradeExecutionService(PortfolioService portfolioService,
                                          TradeEventService tradeEventService) {
        this.portfolioService = portfolioService;
        this.tradeEventService = tradeEventService;
    }

    @Transactional
    @TimeTracked(expectedMillis = 100, shouldPersist = true)
    public void executeTrader(Trader trader, TradingEngine assetTrader) {
        PortfolioAsset traderAsset = assetTrader.getAsset();
        PortfolioAsset previousAsset = PortfolioAsset.from(traderAsset);
        boolean tradeOccurred = assetTrader.trade();
        if (tradeOccurred) {
            log.info("{} Trade executed for trader {} on asset {}. Shares: {} {}, Wallet Dollars: {}, Target Price: {}.",
                PortfolioTraderService.getLogPrefix(trader.getPortfolio().getUser().getSubscriptionTier()),
                trader.getPortfolio().getUser().getId(),
                traderAsset.getCurrency().getCurrencyCode(),
                traderAsset.getShares(),
                traderAsset.getCurrency().getCurrencyCode(),
                traderAsset.getAssetWalletDollars(),
                traderAsset.getTargetPrice());
        }
        updateTraders(trader, assetTrader);
        if (hasAssetChanged(previousAsset, traderAsset)) {
            this.saveAssetChanges(trader, traderAsset, tradeOccurred);
        }
    }

    private void saveAssetChanges(Trader trader, PortfolioAsset traderAsset, boolean tradeOccurred) {
        PortfolioAssetHistory portfolioAssetHistory = new PortfolioAssetHistory(traderAsset, tradeOccurred);
        PortfolioAssetHistory previousPortfolioAssetHistory = this.portfolioService.getLatestPortfolioAssetHistory(traderAsset);
        PortfolioAssetHistory previousWithShares = this.getLastPortfolioAssetWithSharesSinceTime(portfolioAssetHistory);
        this.portfolioService.setPortfolioValueChange(previousPortfolioAssetHistory, portfolioAssetHistory);
        this.portfolioService.setPortfolioSharesChange(previousWithShares, portfolioAssetHistory);
        traderAsset.addPortfolioAssetHistory(portfolioAssetHistory);
        Portfolio traderPortfolio = trader.getPortfolio();
        PortfolioHistory previousPortfolioHistory = this.portfolioService.getLatestPortfolioHistory(traderPortfolio);
        PortfolioHistory portfolioHistory = new PortfolioHistory(traderPortfolio, tradeOccurred);
        this.setValueChange(previousPortfolioHistory, portfolioHistory);
        traderPortfolio.addPortfolioHistory(portfolioHistory);
        this.saveAll(traderAsset, traderPortfolio, portfolioAssetHistory, portfolioHistory);
        if (tradeOccurred) {
            this.saveTradeEvent(portfolioAssetHistory);
        }
    }

    private static boolean hasAssetChanged(PortfolioAsset previousAsset, PortfolioAsset traderAsset) {
        return !previousAsset.equals(traderAsset);
    }

    private static void updateTraders(Trader trader, TradingEngine assetTrader) {
        assetTrader.getAsset().updateValues();
        trader.getPortfolio().updateValues();
    }

    private void saveTradeEvent(PortfolioAssetHistory portfolioAssetHistory) {
        TradeType tradeType = TradeEvent.getTradeType(portfolioAssetHistory);
        TradeEvent tradeEvent = new TradeEvent(portfolioAssetHistory,
            tradeType,
            portfolioAssetHistory.getValueChange(),
            portfolioAssetHistory.getSharesChange());
        this.tradeEventService.saveTradeEvent(tradeEvent);
    }

    private void setValueChange(PortfolioHistory previousPortfolioHistory, PortfolioHistory portfolioHistory) {
        if (previousPortfolioHistory != null) {
            portfolioHistory.calculateValueChange(previousPortfolioHistory);
        } else {
            portfolioHistory.setValueChange(0);
        }
    }

    private PortfolioAssetHistory getLastPortfolioAssetWithSharesSinceTime(PortfolioAssetHistory assetToLookRetrospectively) {
        return this.portfolioService.getLatestPreviousAssetHistoryWithShares(assetToLookRetrospectively);
    }

    @TimeTracked(expectedMillis = 100, shouldPersist = true)
    private void saveAll(PortfolioAsset traderAsset,
                         Portfolio traderPortfolio,
                         PortfolioAssetHistory portfolioAssetHistory,
                         PortfolioHistory portfolioHistory) {
        this.portfolioService.savePortfolioAsset(traderAsset);
        this.portfolioService.savePortfolio(traderPortfolio);
        this.portfolioService.savePortfolioAssetHistory(portfolioAssetHistory);
        this.portfolioService.savePortfolioHistory(portfolioHistory);
    }
}
