package org.cryptotrader.api.library.model.trade;

import lombok.Getter;

@Getter
public abstract class TradeEngine implements TradingEngine {
    private final TradeContext context;

    public TradeEngine() {
        this(TradeContext.ACTUAL);
    }

    public TradeEngine(final TradeContext context) {
        this.context = context;
    }
}
