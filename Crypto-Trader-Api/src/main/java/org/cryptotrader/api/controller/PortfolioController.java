package org.cryptotrader.api.controller;
//=================================-Imports-==================================
import org.cryptotrader.api.library.communication.request.RangedPortfolioHistoryRequest;
import org.cryptotrader.api.library.communication.response.*;
import org.cryptotrader.api.library.services.AuthContextService;
import org.cryptotrader.api.library.services.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.cryptotrader.api.library.communication.request.PortfolioAssetRequest;
import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAssetHistory;
import org.cryptotrader.api.library.entity.portfolio.PortfolioHistory;
import org.cryptotrader.api.library.entity.user.ProductUser;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    //============================-Variables-=================================
    private final PortfolioService portfolioService;
    private final AuthContextService authContextService;
    //===========================-Constructors-===============================
    @Autowired
    public PortfolioController(AuthContextService authContextService,
                               PortfolioService portfolioService) {
        this.authContextService = authContextService;
        this.portfolioService = portfolioService;
    }
    //=============================-Methods-==================================

    //---------------------------Get-Portfolio--------------------------------
    @GetMapping("/get")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PortfolioResponse> getPortfolio(@AuthenticationPrincipal ProductUser user) {
        Portfolio portfolio = this.portfolioService.getPortfolioByUserId(user.getId());
        PortfolioResponse portfolioResponse = new PortfolioResponse(portfolio);
        return ResponseEntity.ok(portfolioResponse);
    }
    //------------------------Add-Portfolio-Asset-----------------------------
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OperationSuccessfulResponse> addPortfolioAsset(@AuthenticationPrincipal ProductUser user,
                                                                         @RequestBody PortfolioAssetRequest portfolioAssetRequest) {
        if (portfolioAssetRequest == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Portfolio portfolio = this.portfolioService.getPortfolioByUserId(user.getId());
        this.portfolioService.addAssetToPortfolio(portfolio, portfolioAssetRequest);
        return ResponseEntity.ok(new OperationSuccessfulResponse(true));
    }
    //-------------------------Is-Empty-Portfolio-----------------------------
    @GetMapping("/empty")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HasPortfolioResponse> emptyPortfolio(@AuthenticationPrincipal ProductUser user) {
        Portfolio portfolio = this.portfolioService.getPortfolioByUserId(user.getId());
        if (portfolio == null) {
            return ResponseEntity.ok(new HasPortfolioResponse(false));
        } else {
            return ResponseEntity.ok(new HasPortfolioResponse(!portfolio.isEmpty()));
        }
    }
    //------------------------Get-Portfolio-History---------------------------
    @GetMapping("/history/get")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PortfolioHistoryResponse>> getPortfolioHistory(@AuthenticationPrincipal ProductUser user) {
        Portfolio portfolio = this.portfolioService.getPortfolioByUserId(user.getId());
        List<PortfolioHistory> portfolioHistory = this.portfolioService.getPortfolioHistory(portfolio);
        if (portfolioHistory.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        List<PortfolioHistoryResponse> historyResponses = portfolioHistory.stream()
                .map(PortfolioHistoryResponse::new)
                .toList();
        return ResponseEntity.ok(historyResponses);
    }
    @PostMapping("/history/ranged/get")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PortfolioHistoryResponse>> getPortfolioHistory(@AuthenticationPrincipal ProductUser user, @RequestBody RangedPortfolioHistoryRequest request) {
        Portfolio portfolio = this.portfolioService.getPortfolioByUserId(user.getId());
        final LocalDateTime startDate = LocalDateTime.parse(request.getStartDate());
        final LocalDateTime endDate = LocalDateTime.parse(request.getEndDate());
        final List<PortfolioHistory> portfolioHistory = this.portfolioService.getRangedPortfolioHistory(portfolio, startDate, endDate);
        if (portfolioHistory.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        final List<PortfolioHistoryResponse> historyResponses = portfolioHistory.stream()
            .map(PortfolioHistoryResponse::new)
            .toList();
        return ResponseEntity.ok(historyResponses);
    }
    @GetMapping("/history/get/asset")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PortfolioAssetHistoryResponse>> getPortfolioAssetHistory(@AuthenticationPrincipal ProductUser user) {
        Portfolio portfolio = this.portfolioService.getPortfolioByUserId(user.getId());
        List<PortfolioAssetHistory> portfolioAssetHistory = this.portfolioService.getPortfolioAssetHistory(portfolio);
        List<PortfolioAssetHistoryResponse> assetHistoryResponses = portfolioAssetHistory.stream()
                                                                                         .map(PortfolioAssetHistoryResponse::new)
                                                                                         .toList();
        return ResponseEntity.ok(assetHistoryResponses);
    }
    @GetMapping("/history/get/asset/{currencyName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PortfolioAssetHistory>> getPortfolioAssetHistoryByCurrency(@AuthenticationPrincipal ProductUser user,
                                                                                          @PathVariable String currencyName) {
        return null;
    }
    //------------------------Get-Portfolio-Profit----------------------------
    @GetMapping("/history/profit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AssetValueResponse> getPortfolioProfit(@AuthenticationPrincipal ProductUser user) {
        Portfolio portfolio = this.portfolioService.getPortfolioByUserId(user.getId());
        double profit = this.portfolioService.getPortfolioProfit(portfolio);
        AssetValueResponse profitResponse = new AssetValueResponse(profit);
        return ResponseEntity.ok(profitResponse);
    }
    //------------------Get-Portfolio-Profit-By-Currency----------------------
    @GetMapping("/history/profit/{currencyName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AssetValueResponse> getPortfolioProfitByCurrency(@AuthenticationPrincipal ProductUser user,
                                                                           @PathVariable String currencyName) {
        if (currencyName == null || currencyName.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Portfolio portfolio = this.portfolioService.getPortfolioByUserId(user.getId());
        PortfolioAsset portfolioAsset = this.portfolioService.getPortfolioAssetByCurrencyName(portfolio, currencyName);
        double profit = this.portfolioService.getPortfolioAssetProfit(portfolioAsset);
        AssetValueResponse profitResponse = new AssetValueResponse(profit);
        return ResponseEntity.ok(profitResponse);
    }
}
