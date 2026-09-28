package org.cryptotrader.api.library.communication.response;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class DisplayCurrencyResponse extends CurrencyValueResponse {
    private String logoUrl;

    public DisplayCurrencyResponse(final String currencyName,
                                   final String currencyCode,
                                   final double value) {
        super(currencyName, currencyCode, value);
        this.logoUrl = this.generateUrl();
    }
    public DisplayCurrencyResponse(final String currencyName,
                                   final String currencyCode,
                                   final double value,
                                   final String logoUrl) {
        super(currencyName, currencyCode, value);
        this.logoUrl = logoUrl;
    }

    public @NotNull String generateUrl() {
        final String baseUrl = "/assets/cryptofont/%s.svg";
        return String.format(baseUrl, this.getCurrencyCode().toLowerCase());
    }
}
