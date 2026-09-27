package org.cryptotrader.api.library.communication.response;

import lombok.Data;

@Data
public class LogoutResponse {
    private boolean isLoggedOut;
    public LogoutResponse(final boolean isLoggedOut) {
        this.isLoggedOut = isLoggedOut;
    }
}