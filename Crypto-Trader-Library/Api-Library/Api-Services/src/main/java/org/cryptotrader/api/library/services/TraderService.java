package org.cryptotrader.api.library.services;

import org.cryptotrader.api.library.communication.response.TradeEventListResponse;
import org.cryptotrader.api.library.communication.response.TradeEventResponse;
import org.cryptotrader.api.library.entity.trade.TradeEvent;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.data.library.services.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraderService {
    private final PortfolioService portfolioService;
    private final CurrencyService currencyService;
    private final TradeEventService tradeEventService;

    @Autowired
    public TraderService(final PortfolioService portfolioService,
                         final CurrencyService currencyService,
                         final TradeEventService tradeEventService) {
        this.portfolioService = portfolioService;
        this.currencyService = currencyService;
        this.tradeEventService = tradeEventService;
    }

    public TradeEventListResponse getTradeEvents(final ProductUser user) {
        final List<TradeEvent> tradeEvents = this.getAllTradeEvents(user);
        return this.toTradeEventListResponse(tradeEvents);
    }

    public TradeEventListResponse getTradeEvents(final ProductUser user, final int offset, final int limit) {
        final List<TradeEvent> tradeEvents = this.tradeEventService.getSelectionByProductUser(user, offset, limit);
        return this.toTradeEventListResponse(tradeEvents);
    }
    public List<TradeEvent> getAllTradeEvents(final ProductUser user) {
        return this.tradeEventService.getAllByProductUser(user);
    }

    public TradeEventListResponse toTradeEventListResponse(final List<TradeEvent> tradeEvents) {
        return new TradeEventListResponse(tradeEvents.stream()
                                                     .map(this::toTradeEventResponse)
                                                     .toList()
        );
    }

    public TradeEventResponse toTradeEventResponse(final TradeEvent tradeEvent) {
        final String currencyName = this.currencyService.getCurrencyName(true,
                                                                   tradeEvent.getAssetHistory().getCurrency());
        return new TradeEventResponse(
                tradeEvent.getId(),
                currencyName,
                tradeEvent.getValueChange(),
                tradeEvent.getSharesChange(),
                tradeEvent.getTradeTime(),
                tradeEvent.getTradeType().getName(),
                tradeEvent.getAssetHistory().getVendor()
        );
    }

    public boolean userHasTrades(final ProductUser user) {
        return this.tradeEventService.userHasTrades(user);
    }
}
