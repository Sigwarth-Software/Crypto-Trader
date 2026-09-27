package org.cryptotrader.universal.library.model;

/**
 * Generic factory interface for building objects.
 *
 * @param <T> type of object constructed by the factory
 * @see org.cryptotrader.api.library.entity.portfolio.builder.models.AbstractPortfolio
 * @see org.cryptotrader.api.library.entity.user.builder.models.AbstractProfilePicture
 * @see org.cryptotrader.api.library.entity.portfolio.Portfolio
 * @see org.cryptotrader.api.library.entity.user.ProductUser
 */
public interface BuilderFactory<T> {
    T build();
}
