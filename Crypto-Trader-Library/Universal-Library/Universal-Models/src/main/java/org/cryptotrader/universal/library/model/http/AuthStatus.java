package org.cryptotrader.universal.library.model.http;

import org.jetbrains.annotations.NotNull;

public enum AuthStatus {
    AUTHORIZED(true),
    UNAUTHORIZED(false);
    public final boolean isAuthorized;
    AuthStatus(final boolean authorized) {
        this.isAuthorized = authorized;
    }
    public static @NotNull AuthStatus from(final boolean authorized) {
        return authorized ? AUTHORIZED : UNAUTHORIZED;
    }
}
