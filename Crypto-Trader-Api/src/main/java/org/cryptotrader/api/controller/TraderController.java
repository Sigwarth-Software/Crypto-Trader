package org.cryptotrader.api.controller;
//=================================-Imports-==================================
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.communication.response.TradeEventListResponse;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.services.AuthContextService;
import org.cryptotrader.api.library.services.TraderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/trader")
public class TraderController {
    private final TraderService traderService;
    private final AuthContextService authContextService;
    
    @Autowired
    public TraderController(TraderService traderService,
                            AuthContextService authContextService) {
        this.traderService = traderService;
        this.authContextService = authContextService;
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/events/all")
    public ResponseEntity<TradeEventListResponse> getAllEvents(@AuthenticationPrincipal ProductUser user) {
        TradeEventListResponse tradeEvents = this.traderService.getTradeEvents(user);
        logTradeEvents(tradeEvents);
        return new ResponseEntity<>(tradeEvents, HttpStatus.OK);
    }
    
    @GetMapping(value = "/events/batch", params = {"offset", "limit"})
    public ResponseEntity<TradeEventListResponse> getBatchEvents(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                                                 @RequestParam(value = "limit", defaultValue = "10") int limit) {
        boolean isAuthorized = this.authContextService.isAuthenticated();
        if (!isAuthorized) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ProductUser user = this.authContextService.getAuthenticatedProductUser();
        TradeEventListResponse tradeEvents = this.traderService.getTradeEvents(user, offset, limit);
        logTradeEvents(tradeEvents);
        return new ResponseEntity<>(tradeEvents, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/events/exists")
    public ResponseEntity<Boolean> hasEvents(@AuthenticationPrincipal ProductUser user) {
        boolean hasEvents = this.traderService.userHasTrades(user);
        return new ResponseEntity<>(hasEvents, HttpStatus.OK);
    }
    
    private static void logTradeEvents(TradeEventListResponse tradeEvents) {
        log.info("Retrieved {} trade events.", tradeEvents.getEvents().size());
    }
}
