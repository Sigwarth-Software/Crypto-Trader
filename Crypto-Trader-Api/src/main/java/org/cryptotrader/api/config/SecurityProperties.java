package org.cryptotrader.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for security-related settings. */
@ConfigurationProperties(prefix = "security.refresh")
public record SecurityProperties(
    String cookieSamesite,
    boolean cookieSecure
) {
    public SecurityProperties {
        if (cookieSamesite == null) {
            cookieSamesite = "Strict";
        }
    }
}
