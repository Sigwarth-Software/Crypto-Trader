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
    public PortfolioService(PortfolioRepository portfolioRepository,
                            PortfolioAssetRepository portfolioAssetRepository,
                            PortfolioHistoryRepository portfolioHistoryRepository,
                            PortfolioAssetHistoryRepository portfolioAssetHistoryRepository,
                            CurrencyService currencyService,
                            PortfolioEntityService portfolioEntityService,
                            PortfolioAssetEntityService portfolioAssetEntityService,
                            PortfolioHistoryEntityService portfolioHistoryEntityService,
                            PortfolioAssetHistoryEntityService portfolioAssetHistoryEntityService) {
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
    public void setPortfolioValueChange(PortfolioAssetHistory previousPortfolioAssetHistory,
                                        PortfolioAssetHistory portfolioAssetHistory) {
        if (previousPortfolioAssetHistory != null) {
            portfolioAssetHistory.calculateValueChange(previousPortfolioAssetHistory);
        } else {
            portfolioAssetHistory.setValueChange(0);
        }
    }

    public void setPortfolioSharesChange(PortfolioAssetHistory previousAssetWithShares,
                                         PortfolioAssetHistory portfolioAssetHistory) {
        if (previousAssetWithShares != null) {
            portfolioAssetHistory.calculateShareChange(previousAssetWithShares);
        } else {
            portfolioAssetHistory.setSharesChange(0);
        }
    }

    public PortfolioAssetHistoryResponse toPortfolioAssetHistoryResponse(PortfolioAssetHistory portfolioAssetHistory) {
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

    public PortfolioHistoryResponse toPortfolioHistoryResponse(PortfolioHistory portfolioHistory) {
        return new PortfolioHistoryResponse(portfolioHistory);
    }

    public List<PortfolioAssetHistoryResponse> toHistoryResponses(List<PortfolioAssetHistory> assetHistories) {
        return assetHistories.stream()
                .map(this::toPortfolioAssetHistoryResponse)
                .toList();
    }

    public List<PortfolioAssetHistory> getPortfolioAssetHistory(Portfolio portfolio) {
        return this.portfolioAssetHistoryRepository.findAllByPortfolioId(portfolio.getId());
    }
    public PortfolioAssetHistory getLatestPortfolioAssetHistory(PortfolioAsset portfolioAsset) {
        return this.portfolioAssetHistoryRepository.findFirstByPortfolioAssetIdOrderByLastUpdatedDesc(portfolioAsset.getId());
    }

    /**
     * Find the most recent preceding history entry for the same asset where shares != 0.
     * Returns null when input is incomplete or no such entry exists.
     */
    public PortfolioAssetHistory getLatestPreviousAssetHistoryWithShares(PortfolioAssetHistory currentHistory) {
        if (currentHistory == null || currentHistory.getPortfolioAsset() == null || currentHistory.getLastUpdated() == null) {
            return null;
        }
        Long assetId = currentHistory.getPortfolioAsset().getId();
        List<PortfolioAssetHistory> list = this.portfolioAssetHistoryRepository.findLatestWithSharesBefore(assetId, currentHistory.getLastUpdated());
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }
    public PortfolioHistory getLatestPortfolioHistory(Portfolio portfolio) {
        return this.portfolioHistoryRepository.findFirstByPortfolioIdOrderByLastUpdatedDesc(portfolio.getId());
    }
    //---------------------Save-Portfolio-Asset-History-----------------------
    public void savePortfolioAssetHistory(PortfolioAssetHistory portfolioAssetHistory) {
        this.portfolioAssetHistoryEntityService.save(portfolioAssetHistory);
    }
    //----------------------Save-Portfolio-History----------------------------
    public void savePortfolioHistory(PortfolioHistory portfolioHistory) {
        this.portfolioHistoryEntityService.save(portfolioHistory);
    }

    //---------------------------Save-Portfolio-------------------------------
    @Transactional
    public void savePortfolio(Portfolio portfolio) {
        this.portfolioEntityService.save(portfolio);
    }
    //------------------------Save-Portfolio-Asset----------------------------
    @Transactional
    public void savePortfolioAsset(PortfolioAsset portfolioAsset) {
        this.portfolioAssetEntityService.save(portfolioAsset);
    }
    //----------------------Get-Portfolio-By-User-Id--------------------------
    @Transactional(readOnly = true)
    public Portfolio getPortfolioByUserId(Long userId) {
        return this.portfolioRepository.findPortfolioByUserId(userId);
    }
    //-------------------------Get-All-Portfolios-----------------------------
    public List<Portfolio> getAllPortfolios() {
        return this.portfolioEntityService.findAll();
    }
    //-----------------------Add-Asset-To-Portfolio---------------------------
    @Transactional
    public void addAssetToPortfolio(Portfolio portfolio, PortfolioAssetRequest portfolioAssetRequest) {
        Currency requestCurrency = this.currencyService.getCurrencyByName(portfolioAssetRequest.getCurrencyName());
        PortfolioAsset portfolioAsset = new PortfolioAsset(portfolio, requestCurrency, portfolioAssetRequest.getShares(), portfolioAssetRequest.getWalletDollars());
        this.portfolioEntityService.save(portfolio);
        this.portfolioAssetEntityService.save(portfolioAsset);
    }
    //-----------------------Get-Portfolio-History----------------------------
    public List<PortfolioHistory> getPortfolioHistory(Portfolio portfolio) {
        return this.getPortfolioHistory(portfolio.getId());
    }

    public List<PortfolioHistory> getPortfolioHistory(Long portfolioId) {
        return this.portfolioHistoryRepository.findAllByPortfolioId(portfolioId);
    }

    public List<PortfolioAsset> getAssetsByPortfolio(Long portfolioId) {
        return this.portfolioAssetRepository.findAllByPortfolioId(portfolioId);
    }
    //------------------------Get-Portfolio-Profit----------------------------
    public double getPortfolioProfit(Portfolio portfolio) {
        PortfolioHistory initialPortfolioHistory = this.portfolioHistoryRepository.getFirstByPortfolioId(portfolio.getId());
        if (initialPortfolioHistory == null) {
            return 0;
        }
        double profit = portfolio.getTotalWorth() - initialPortfolioHistory.getTotalWorth();
        return profit;
    }
    //---------------------Get-Portfolio-Asset-Profit-------------------------
    public double getPortfolioAssetProfit(PortfolioAsset portfolioAsset) {
        PortfolioAssetHistory initialPortfolioAssetHistory = this.portfolioAssetHistoryRepository.getFirstByPortfolioAssetId(portfolioAsset.getId());
        if (initialPortfolioAssetHistory == null) {
            return 0;
        }
        double profit = portfolioAsset.getTotalValueInDollars() - initialPortfolioAssetHistory.getTotalValueInDollars();
        return profit;
    }
    //----------------Get-Portfolio-Asset-By-Currency-Name--------------------
    public PortfolioAsset getPortfolioAssetByCurrencyName(Portfolio portfolio, String currencyName) {
        return this.portfolioAssetRepository.getPortfolioAssetByPortfolioIdAndCurrencyName(portfolio.getId(), currencyName);
    }

    public Optional<PortfolioAsset> getPortfolioAssetByHistory(PortfolioAssetHistory portfolioAssetHistory) {
        Long portfolioAssetId = portfolioAssetHistory.getPortfolioAsset().getId();
        return this.portfolioAssetEntityService.findById(portfolioAssetId);
    }

    public ProductUser getProductUserByAsset(PortfolioAsset portfolioAsset) {
        return portfolioAsset.getPortfolio().getUser();
    }

    public Portfolio getInitializedPortfolio(Long userId) {
        Portfolio portfolio = this.getPortfolioByUserId(userId);

        if (portfolio == null) {
            return new Portfolio();
        }

        Hibernate.initialize(portfolio.getAssets());

        return portfolio;
    }
}