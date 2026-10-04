package org.cryptotrader.data.library.entity.currency.builder.models;

import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.universal.library.model.BuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * Abstract base class for building {@link Currency} instances.
 *
 * @see Currency
 * @see BuilderFactory
 */
public abstract class AbstractCurrency implements BuilderFactory<Currency> {
    public abstract @NotNull AbstractCurrency name(@Nullable String name);

    public abstract @NotNull AbstractCurrency currencyCode(@Nullable String currencyCode);

    public abstract @NotNull AbstractCurrency urlPath(@Nullable String urlPath);

    public abstract @NotNull AbstractCurrency value(double value);

    public abstract @NotNull AbstractCurrency lastUpdated(@Nullable LocalDateTime lastUpdated);
}
