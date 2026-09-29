package org.cryptotrader.data.library.entity.currency.builder;

import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.data.library.entity.currency.builder.models.AbstractCurrency;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/** A builder factory for creating currency entities. */
public class CurrencyBuilder extends AbstractCurrency {
    private String name;
    private String currencyCode;
    private String urlPath;
    private double value;
    private LocalDateTime lastUpdated;

    public CurrencyBuilder() {
        this.name = "";
        this.currencyCode = "";
        this.urlPath = "";
        this.value = 0;
        this.lastUpdated = LocalDateTime.now();
    }
    @Override
    public @NotNull CurrencyBuilder name(final String name) {
        this.name = name;
        return this;
    }
    @Override
    public @NotNull CurrencyBuilder currencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }
    @Override
    public @NotNull CurrencyBuilder urlPath(final String urlPath) {
        this.urlPath = urlPath;
        return this;
    }
    @Override
    public @NotNull CurrencyBuilder value(final double value) {
        this.value = value;
        return this;
    }
    @Override
    public @NotNull CurrencyBuilder lastUpdated(final LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }
    @Override
    public @NotNull Currency build() {
        return new Currency(this.name,
                            this.currencyCode,
                            this.urlPath,
                            this.value,
                            this.lastUpdated);
    }
}
