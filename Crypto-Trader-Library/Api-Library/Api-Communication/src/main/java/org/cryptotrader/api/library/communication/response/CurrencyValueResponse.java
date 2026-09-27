package org.cryptotrader.api.library.communication.response;

import lombok.Data;

@Data
public class CurrencyValueResponse {
    private String currencyName;
    private String currencyCode;
    private double value;
    public CurrencyValueResponse(final String currencyName,
                                 final String currencyCode,
                                 final double value) {
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.value = value;
    }
}
