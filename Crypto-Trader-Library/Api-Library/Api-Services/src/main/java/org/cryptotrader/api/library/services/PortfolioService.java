package org.cryptotrader.api.library.services;
//=================================-Imports-==================================

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.communication.request.PortfolioAssetRequest;
import org.cryptotrader.api.library.communication.response.PortfolioAssetHistoryResponse;
import org.cryptotrader.api.library.communication.response.PortfolioHistoryResponse;
import org.cryptotrader.api.library.services.entity.portfolio.PortfolioAssetEntityService;
import org.cryptotrader.api.library.services.entity.portfolio.PortfolioAssetHistoryEntityService;
import org.cryptotrader.api.library.services.entity.portfolio.PortfolioEntityService;
import org.cryptotrader.api.library.services.entity.portfolio.PortfolioHistoryEntityService;
import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAssetHistory;
import org.cryptotrader.api.library.entity.portfolio.PortfolioHistory;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.repository.PortfolioAssetHistoryRepository;
import org.cryptotrader.api.library.repository.PortfolioAssetRepository;
import org.cryptotrader.api.library.repository.PortfolioHistoryRepository;
import org.cryptotrader.api.library.repository.PortfolioRepository;
import org.cryptotrader.data.library.services.CurrencyService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PortfolioService {
    //============================-Variables-=================================
    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final PortfolioHistoryRepository portfolioHistoryRepository;
    private final PortfolioAssetHistoryRepository portfolioAssetHistoryRepository;
    private final CurrencyService currencyService;

    private final PortfolioEntityService portfolioEntityService;
    private final PortfolioAssetEntityService portfolioAssetEntityService;
    private final PortfolioHistoryEntityService portfolioHistoryEntityService;
    private final PortfolioAssetHistoryEntityService portfolioAssetHistoryEntityService;
    //===========================-Constructors-===============================
    @Autowired
    public PortfolioService(final PortfolioRepository portfolioRepository,
                            final PortfolioAssetRepository portfolioAssetRepository,
                            final PortfolioHistoryRepository portfolioHistoryRepository,
                            final PortfolioAssetHistoryRepository portfolioAssetHistoryRepository,
                            final CurrencyService currencyService,
                            final PortfolioEntityService portfolioEntityService,
                            final PortfolioAssetEntityService portfolioAssetEntityService,
                            final PortfolioHistoryEntityService portfolioHistoryEntityService,
                            final PortfolioAssetHistoryEntityService portfolioAssetHistoryEntityService) {
        this.currencyService = currencyService;
        this.portfolioRepository = portfolioRepository;
        this.portfolioAssetRepository = portfolioAssetRepository;
        this.portfolioHistoryRepository = portfolioHistoryRepository;
        this.portfolioAssetHistoryRepository = portfolioAssetHistoryRepository;
        this.portfolioEntityService = portfolioEntityService;
        this.portfolioAssetEntityService = portfolioAssetEntityService;
        this.portfolioHistoryEntityService = portfolioHistoryEntityService;
        this.portfolioAssetHistoryEntityService = portfolioAssetHistoryEntityService;
    }
    //============================-Methods-===================================


    /**
     * Calculate and set value and share deltas on the current history entry using an optional previous entry.
     * If no previous is provided, the current entry's valueChange is set to 0.
     */
    public void setPortfolioValueChange(final PortfolioAssetHistory previousPortfolioAssetHistory,
                                        final PortfolioAssetHistory portfolioAssetHistory) {
        if (previousPortfolioAssetHistory != null) {
            portfolioAssetHistory.calculateValueChange(previousPortfolioAssetHistory);
        } else {
            portfolioAssetHistory.setValueChange(0);
        }
    }

    public void setPortfolioSharesChange(final PortfolioAssetHistory previousAssetWithShares,
                                         final PortfolioAssetHistory portfolioAssetHistory) {
        if (previousAssetWithShares != null) {
            portfolioAssetHistory.calculateShareChange(previousAssetWithShares);
        } else {
            portfolioAssetHistory.setSharesChange(0);
        }
    }

    public PortfolioAssetHistoryResponse toPortfolioAssetHistoryResponse(final PortfolioAssetHistory portfolioAssetHistory) {
        return new PortfolioAssetHistoryResponse(
                portfolioAssetHistory.getCurrency().getCurrencyCode(),
                portfolioAssetHistory.getShares(),
                portfolioAssetHistory.getSharesValueInDollars(),
                portfolioAssetHistory.getAssetWalletDollars(),
                portfolioAssetHistory.getTargetPrice(),
                portfolioAssetHistory.getValueChange(),
                portfolioAssetHistory.getSharesChange(),
                portfolioAssetHistory.isTradeOccurred(),
                portfolioAssetHistory.getVendor().getName(),
                portfolioAssetHistory.getVendor().getRate(),
                portfolioAssetHistory.getLastUpdated().toString()
        );
    }

    public PortfolioHistoryResponse toPortfolioHistoryResponse(final PortfolioHistory portfolioHistory) {
        return new PortfolioHistoryResponse(portfolioHistory);
    }

    public List<PortfolioAssetHistoryResponse> toHistoryResponses(final List<PortfolioAssetHistory> assetHistories) {
        return assetHistories.stream()
                .map(this::toPortfolioAssetHistoryResponse)
                .toList();
    }

    public List<PortfolioAssetHistory> getPortfolioAssetHistory(final Portfolio portfolio) {
        return this.portfolioAssetHistoryRepository.findAllByPortfolioId(portfolio.getId());
    }
    public PortfolioAssetHistory getLatestPortfolioAssetHistory(final PortfolioAsset portfolioAsset) {
        return this.portfolioAssetHistoryRepository.findFirstByPortfolioAssetIdOrderByLastUpdatedDesc(portfolioAsset.getId());
    }

    /**
     * Find the most recent preceding history entry for the same asset where shares != 0.
     * Returns null when input is incomplete or no such entry exists.
     */
    public PortfolioAssetHistory getLatestPreviousAssetHistoryWithShares(final PortfolioAssetHistory currentHistory) {
        if (currentHistory == null || currentHistory.getPortfolioAsset() == null || currentHistory.getLastUpdated() == null) {
            return null;
        }
        final Long assetId = currentHistory.getPortfolioAsset().getId();
        final List<PortfolioAssetHistory> list = this.portfolioAssetHistoryRepository.findLatestWithSharesBefore(assetId, currentHistory.getLastUpdated());
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }
    public PortfolioHistory getLatestPortfolioHistory(final Portfolio portfolio) {
        return this.portfolioHistoryRepository.findFirstByPortfolioIdOrderByLastUpdatedDesc(portfolio.getId());
    }
    //---------------------Save-Portfolio-Asset-History-----------------------
    public void savePortfolioAssetHistory(final PortfolioAssetHistory portfolioAssetHistory) {
        this.portfolioAssetHistoryEntityService.save(portfolioAssetHistory);
    }
    //----------------------Save-Portfolio-History----------------------------
    public void savePortfolioHistory(final PortfolioHistory portfolioHistory) {
        this.portfolioHistoryEntityService.save(portfolioHistory);
    }

    //---------------------------Save-Portfolio-------------------------------
    @Transactional
    public void savePortfolio(final Portfolio portfolio) {
        this.portfolioEntityService.save(portfolio);
    }
    //------------------------Save-Portfolio-Asset----------------------------
    @Transactional
    public void savePortfolioAsset(final PortfolioAsset portfolioAsset) {
        this.portfolioAssetEntityService.save(portfolioAsset);
    }
    //----------------------Get-Portfolio-By-User-Id--------------------------
    @Transactional(readOnly = true)
    public Portfolio getPortfolioByUserId(final Long userId) {
        return this.portfolioRepository.findPortfolioByUserId(userId);
    }
    //-------------------------Get-All-Portfolios-----------------------------
    public List<Portfolio> getAllPortfolios() {
        return this.portfolioEntityService.findAll();
    }
    //-----------------------Add-Asset-To-Portfolio---------------------------
    @Transactional
    public void addAssetToPortfolio(final Portfolio portfolio, final PortfolioAssetRequest portfolioAssetRequest) {
        final Currency requestCurrency = this.currencyService.getCurrencyByName(portfolioAssetRequest.getCurrencyName());
        final PortfolioAsset portfolioAsset = new PortfolioAsset(portfolio, requestCurrency, portfolioAssetRequest.getShares(), portfolioAssetRequest.getWalletDollars());
        this.portfolioEntityService.save(portfolio);
        this.portfolioAssetEntityService.save(portfolioAsset);
    }
    //-----------------------Get-Portfolio-History----------------------------
    public List<PortfolioHistory> getPortfolioHistory(final Portfolio portfolio) {
        return this.getPortfolioHistory(portfolio.getId());
    }

    public List<PortfolioHistory> getRangedPortfolioHistory(final Portfolio portfolio, final LocalDateTime startDate, final LocalDateTime endDate) {
        return this.portfolioHistoryRepository.findAllByPortfolioIdAndLastUpdatedBetweenOrderByLastUpdatedAsc(portfolio.getId(), startDate, endDate);
    }

    public List<PortfolioHistory> getPortfolioHistory(final Long portfolioId) {
        return this.portfolioHistoryRepository.findAllByPortfolioIdOrderByLastUpdatedAsc(portfolioId);
    }

    public List<PortfolioAsset> getAssetsByPortfolio(final Long portfolioId) {
        return this.portfolioAssetRepository.findAllByPortfolioId(portfolioId);
    }
    //------------------------Get-Portfolio-Profit----------------------------
    public double getPortfolioProfit(final Portfolio portfolio) {
        final PortfolioHistory initialPortfolioHistory = this.portfolioHistoryRepository.getFirstByPortfolioId(portfolio.getId());
        if (initialPortfolioHistory == null) {
            return 0;
        }
        final double profit = portfolio.getTotalWorth() - initialPortfolioHistory.getTotalWorth();
        return profit;
    }
    //---------------------Get-Portfolio-Asset-Profit-------------------------
    public double getPortfolioAssetProfit(final PortfolioAsset portfolioAsset) {
        final PortfolioAssetHistory initialPortfolioAssetHistory = this.portfolioAssetHistoryRepository.getFirstByPortfolioAssetId(portfolioAsset.getId());
        if (initialPortfolioAssetHistory == null) {
            return 0;
        }
        final double profit = portfolioAsset.getTotalValueInDollars() - initialPortfolioAssetHistory.getTotalValueInDollars();
        return profit;
    }
    //----------------Get-Portfolio-Asset-By-Currency-Name--------------------
    public PortfolioAsset getPortfolioAssetByCurrencyName(final Portfolio portfolio, final String currencyName) {
        return this.portfolioAssetRepository.getPortfolioAssetByPortfolioIdAndCurrencyName(portfolio.getId(), currencyName);
    }

    public Optional<PortfolioAsset> getPortfolioAssetByHistory(final PortfolioAssetHistory portfolioAssetHistory) {
        final Long portfolioAssetId = portfolioAssetHistory.getPortfolioAsset().getId();
        return this.portfolioAssetEntityService.findById(portfolioAssetId);
    }

    public ProductUser getProductUserByAsset(final PortfolioAsset portfolioAsset) {
        return portfolioAsset.getPortfolio().getUser();
    }

    public Portfolio getInitializedPortfolio(final Long userId) {
        final Portfolio portfolio = this.getPortfolioByUserId(userId);

        if (portfolio == null) {
            return new Portfolio();
        }

        Hibernate.initialize(portfolio.getAssets());

        return portfolio;
    }
}
