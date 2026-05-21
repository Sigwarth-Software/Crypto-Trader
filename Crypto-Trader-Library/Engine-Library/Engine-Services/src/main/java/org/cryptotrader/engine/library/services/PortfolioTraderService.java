package org.cryptotrader.engine.library.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.user.SubscriptionTier;
import org.cryptotrader.api.library.model.trade.CryptoTrader;
import org.cryptotrader.api.library.model.trade.Trader;
import org.cryptotrader.api.library.model.trade.TradingEngine;
import org.cryptotrader.api.library.services.PortfolioService;
import org.jspecify.annotations.NonNull;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "cryptotrader.engine.trading.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class PortfolioTraderService {
    private final PortfolioService portfolioService;
    private List<Portfolio> allUsersPortfolios;
    private final CryptoTrader cryptoTrader;
    private final PlatformTransactionManager transactionManager;
    private final PortfolioTradeExecutionService portfolioTradeExecutionService;

    @Autowired
    public PortfolioTraderService(PortfolioService portfolioService,
                                  CryptoTrader cryptoTrader,
                                  PlatformTransactionManager transactionManager,
                                  PortfolioTradeExecutionService portfolioTradeExecutionService) {
        this.portfolioService = portfolioService;
        this.cryptoTrader = cryptoTrader;
        this.transactionManager = transactionManager;
        this.portfolioTradeExecutionService = portfolioTradeExecutionService;
    }

    @PostConstruct
    public void init() {
        new TransactionTemplate(this.transactionManager).execute(status -> {
            this.initPortfolios();
            this.initCryptoTrader();
            return null;
        });
    }

    private void initCryptoTrader() {
        for (Portfolio portfolio : this.allUsersPortfolios) {
            this.cryptoTrader.addTrader(new Trader(portfolio));
        }
    }

    private void initPortfolios() {
        this.allUsersPortfolios = this.portfolioService.getAllPortfolios();
        if (this.allUsersPortfolios == null) {
            this.allUsersPortfolios = new ArrayList<>();
        }
    }

    //--------------------------Trade-Portfolios------------------------------
    public void tradePortfolios(List<Portfolio> portfolios, SubscriptionTier subscriptionTier) {
        final String logPrefix = getLogPrefix(subscriptionTier);
        if (portfolios == null || portfolios.isEmpty()) {
            log.info("{} No traders found. No trades will be made.", logPrefix);
            this.fillPortfolioList();
            return;
        }
        log.info("{} Traders found. Trades are being be made.", logPrefix);
        this.triggerAllTraders(this.cryptoTrader.getTradersBySubscriptionTier(subscriptionTier));
    }

    static @NonNull String getLogPrefix(SubscriptionTier subscriptionTier) {
        final String resetAnsi = "\u001B[0m";
        final String purpleAnsi = "\u001B[35m";
        final String greenAnsi = "\u001B[32m";
        final String blueAnsi = "\u001B[34m";
        final String chosenAnsi = switch (subscriptionTier) {
            case FREE -> greenAnsi;
            case PRO -> blueAnsi;
            case ULTIMATE -> purpleAnsi;
        };
        final String logPrefix = "%s[%s]%s".formatted(chosenAnsi, subscriptionTier.getLabel().toUpperCase(), resetAnsi);
        return logPrefix;
    }

    @Transactional
    @Scheduled(fixedRate = 10_000)
    public void tradeFreePortfolios() {
        this.fillPortfolioList();
        List<Portfolio> portfolios = this.filterBySubscriptionTier(this.allUsersPortfolios, SubscriptionTier.FREE);
        this.tradePortfolios(portfolios, SubscriptionTier.FREE);
    }

    @Transactional
    @Scheduled(fixedRate = 5000)
    public void tradeProPortfolios() {
        this.fillPortfolioList();
        List<Portfolio> portfolios = this.filterBySubscriptionTier(this.allUsersPortfolios, SubscriptionTier.PRO);
        this.tradePortfolios(portfolios, SubscriptionTier.PRO);
    }

    @Transactional
    @Scheduled(fixedRate = 1000)
    public void tradeUltimatePortfolios() {
        this.fillPortfolioList();
        List<Portfolio> portfolios = this.filterBySubscriptionTier(this.allUsersPortfolios, SubscriptionTier.ULTIMATE);
        this.tradePortfolios(portfolios, SubscriptionTier.ULTIMATE);
    }

    public List<Portfolio> filterBySubscriptionTier(List<Portfolio> portfolios, SubscriptionTier tier) {
        List<Portfolio> portfoliosByTier = portfolios.stream()
                .filter(portfolio -> portfolio.getUser().getSubscriptionTier() == tier)
                .toList();
        return portfoliosByTier;
    }

    private void fillPortfolioList() {
        log.debug("Refreshing portfolio list for trading.");
        this.cryptoTrader.getTraders().clear();
        this.allUsersPortfolios = this.portfolioService.getAllPortfolios();
        this.cryptoTrader.addAllPortfolios(this.allUsersPortfolios);
    }

    public void triggerAllTraders() {
        this.triggerAllTraders(this.cryptoTrader.getTraders());
    }

    @TimeTracked(expectedMillis = 2000, shouldPersist = true)
    public void triggerAllTraders(List<Trader> traders) {
        for (Trader trader : traders) {
            for (TradingEngine assetTrader : trader.getAssetTraders()) {
                this.portfolioTradeExecutionService.executeTrader(trader, assetTrader);
            }
        }
    }

    //----------------------Add-Portfolio-To-Traders--------------------------
    public void addPortfolioToTraders(Portfolio portfolio) {
        this.cryptoTrader.addTrader(new Trader(portfolio));
    }
}
