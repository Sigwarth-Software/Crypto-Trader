package org.cryptotrader.universal.library.events.model;

public interface Publisher {
    <T> void publish(T event);
}
