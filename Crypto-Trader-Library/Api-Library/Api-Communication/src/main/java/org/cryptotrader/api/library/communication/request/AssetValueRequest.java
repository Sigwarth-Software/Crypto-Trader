package org.cryptotrader.api.library.communication.request;

import lombok.Data;

@Data
public class AssetValueRequest {
    private String currencyCode;
    private double shares;
    public AssetValueRequest(final String currencyCode, final double shares) {
        this.currencyCode = currencyCode;
        this.shares = shares;
    }
}
