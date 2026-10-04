package org.cryptotrader.security.library.entity.key;

public enum KeyType {
    CRYPTO_TRADER_KEY("crypto_trader_key"),
    COINBASE_API_KEY("coinbase_api_key"),
    BINANCE_API_KEY("binance_api_key");
    private final String keyName;
    KeyType(final String keyName) {
        this.keyName = keyName;
    }
}