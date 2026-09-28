package org.cryptotrader.engine.library.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.user.SubscriptionTier;
import org.cryptotrader.api.library.model.trade.CryptoTrader;
import org.cryptotrader.api.library.model.trade.Trader;
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
    public PortfolioTraderService(final PortfolioService portfolioService,
                                  final CryptoTrader cryptoTrader,
                                  final PlatformTransactionManager transactionManager,
                                  final PortfolioTradeExecutionService portfolioTradeExecutionService) {
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
        for (final Portfolio portfolio : this.allUsersPortfolios) {
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
    public void tradePortfolios(final List<Portfolio> portfolios, final SubscriptionTier subscriptionTier) {
        final String logPrefix = getLogPrefix(subscriptionTier);
        if (portfolios == null || portfolios.isEmpty()) {
            log.info("{} No traders found. No trades will be made.", logPrefix);
            this.fillPortfolioList();
            return;
        }
        log.info("{} Traders found. Trades are being be made.", logPrefix);
        this.triggerAllTraders(this.cryptoTrader.getTradersBySubscriptionTier(subscriptionTier));
    }

    static @NonNull String getLogPrefix(final SubscriptionTier subscriptionTier) {
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
    @Scheduled(fixedRate = 30_000)
    public void tradeFreePortfolios() {
        this.fillPortfolioList();
        final List<Portfolio> portfolios = this.filterBySubscriptionTier(this.allUsersPortfolios, SubscriptionTier.FREE);
        this.tradePortfolios(portfolios, SubscriptionTier.FREE);
    }

    @Transactional
    @Scheduled(fixedRate = 10_000)
    public void tradeProPortfolios() {
        this.fillPortfolioList();
        final List<Portfolio> portfolios = this.filterBySubscriptionTier(this.allUsersPortfolios, SubscriptionTier.PRO);
        this.tradePortfolios(portfolios, SubscriptionTier.PRO);
    }

    @Transactional
    @Scheduled(fixedRate = 5000)
    public void tradeUltimatePortfolios() {
        this.fillPortfolioList();
        final List<Portfolio> portfolios = this.filterBySubscriptionTier(this.allUsersPortfolios, SubscriptionTier.ULTIMATE);
        this.tradePortfolios(portfolios, SubscriptionTier.ULTIMATE);
    }

    public List<Portfolio> filterBySubscriptionTier(final List<Portfolio> portfolios, final SubscriptionTier tier) {
        final List<Portfolio> portfoliosByTier = portfolios.stream()
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
    public void triggerAllTraders(final List<Trader> traders) {
        this.portfolioTradeExecutionService.triggerAllTraders(traders);
    }

    //----------------------Add-Portfolio-To-Traders--------------------------
    public void addPortfolioToTraders(final Portfolio portfolio) {
        this.cryptoTrader.addTrader(new Trader(portfolio));
    }
}
