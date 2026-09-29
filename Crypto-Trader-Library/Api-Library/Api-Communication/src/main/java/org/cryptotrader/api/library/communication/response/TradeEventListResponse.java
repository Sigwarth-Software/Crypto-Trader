package org.cryptotrader.api.library.communication.response;

import lombok.Data;

import java.util.List;

@Data
public class TradeEventListResponse {
    private List<TradeEventResponse> events;
    public TradeEventListResponse(final List<TradeEventResponse> events) {
        this.events = events;
    }
}
