package org.cryptotrader.api.controller;
//=================================-Imports-==================================
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.communication.response.SubscriptionTierResponse;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.services.PortfolioService;
import org.cryptotrader.api.library.services.ProductUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.cryptotrader.api.library.entity.user.User;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {
    //============================-Variables-=================================
    private final ProductUserService productUserService;
    private final PortfolioService portfolioService;
    //===========================-Constructors-===============================
    @Autowired
    public UserController(ProductUserService productUserService, PortfolioService portfolioService) {
        this.productUserService = productUserService;
        this.portfolioService = portfolioService;
    }
    //=============================-Methods-==================================

    @GetMapping("/tier")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionTierResponse> getMySubscriptionTier(@AuthenticationPrincipal ProductUser user) {
        return ResponseEntity.ok(new SubscriptionTierResponse(user.getSubscriptionTier()));
    }

}
