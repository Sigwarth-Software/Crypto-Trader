package org.cryptotrader.api.library.model.trade;

public enum TradeContext {
    ACTUAL("actual"),
    SIMULATION("simulation");

    private final String contextType;

    TradeContext(String contextType) {
        this.contextType = contextType;
    }

    public String getContextType() {
        return this.contextType;
    }

    public boolean isSimulation() {
        return this == SIMULATION;
    }

    public boolean isActual() {
        return this == ACTUAL;
    }
}
