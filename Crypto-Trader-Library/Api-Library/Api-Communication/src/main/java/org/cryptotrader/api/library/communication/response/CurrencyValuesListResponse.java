package org.cryptotrader.api.library.communication.response;

import lombok.Data;

import java.util.List;

@Data
public class CurrencyValuesListResponse {
    private List<CurrencyValueResponse> currencies;
    public CurrencyValuesListResponse(final List<CurrencyValueResponse> currencies) {
        this.currencies = currencies;
    }
}
