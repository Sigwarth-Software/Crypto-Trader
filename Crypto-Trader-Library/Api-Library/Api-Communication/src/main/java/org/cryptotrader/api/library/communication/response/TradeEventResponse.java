package org.cryptotrader.api.library.communication.response;

import lombok.Data;
import org.cryptotrader.api.library.entity.vendor.Vendor;

import java.time.LocalDateTime;

@Data
public class TradeEventResponse {
    private Long id;
    private String currency;
    private double valueChange;
    private double sharesChange;
    private String tradeTime;
    private String tradeType;
    private String vendor;

    public TradeEventResponse(final Long id,
                              final String currency,
                              final double valueChange,
                              final double sharesChange,
                              final LocalDateTime tradeTime,
                              final String tradeType,
                              final Vendor vendor) {
        this.id = id;
        this.currency = currency;
        this.valueChange = valueChange;
        this.sharesChange = sharesChange;
        this.tradeTime = tradeTime.toString();
        this.tradeType = tradeType;
        this.vendor = vendor.getName();
    }
}