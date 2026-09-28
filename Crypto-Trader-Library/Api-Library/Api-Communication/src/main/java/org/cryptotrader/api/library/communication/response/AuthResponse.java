package org.cryptotrader.api.library.communication.response;

import lombok.Data;
import org.cryptotrader.universal.library.model.http.AuthStatus;
import org.jetbrains.annotations.NotNull;

@Data
public class AuthResponse {
    private boolean authorized;
    private String token;

    public AuthResponse(final @NotNull AuthStatus authStatus) {
        this.authorized = authStatus.isAuthorized;
    }

    public AuthResponse(final @NotNull AuthStatus authStatus, final String token) {
        this.authorized = authStatus.isAuthorized;
        this.token = token;
    }

    public AuthResponse(final boolean isAuthorized) {
        this.authorized = isAuthorized;
    }

    public AuthResponse(final boolean isAuthorized, final String token) {
        this.authorized = isAuthorized;
        this.token = token;
    }
}
