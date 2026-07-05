package org.cryptotrader.universal.library.entity;

public abstract class Identifiable<T> {
    public abstract T getId();
    public abstract void setId(T id);
}
