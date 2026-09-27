package org.cryptotrader.universal.library.model.http;

public enum AuthStatus {
    AUTHORIZED(true),
    UNAUTHORIZED(false);
    public final boolean isAuthorized;
    AuthStatus(final boolean authorized) {
        this.isAuthorized = authorized;
    }
    public static AuthStatus from(final boolean authorized) {
        return authorized ? AUTHORIZED : UNAUTHORIZED;
    }
}
