package org.cryptotrader.api.library.model.portfolio;

public interface SequentiallyValuable<T> {
    void calculateValueChange(T previous);
}
