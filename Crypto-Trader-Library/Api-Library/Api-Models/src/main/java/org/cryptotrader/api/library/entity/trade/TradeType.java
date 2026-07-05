package org.cryptotrader.api.library.entity.trade;

// TODO: There is some duplication. Choose one, or specialize.
public enum TradeType {
    BUY("Buy"),
    SELL("Sell");

    private final String name;

    private TradeType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}