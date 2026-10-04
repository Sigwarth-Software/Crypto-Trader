package org.cryptotrader.logging.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "cryptotrader.http.logging")
public class CryptoTraderHttpLoggingProperties {
    private boolean enabled = true;
    private boolean includeQueryString = true;
    private boolean includePayload = true; // request payload
    private int maxPayloadLength = 512;    // request payload max length
    private boolean includeHeaders = false;

    // New options
    private boolean includeResponsePayload = false;
    private int maxResponsePayloadLength = 512;
    private boolean colorEnabled = true;

    // Kept for backward compatibility (not used by the new filter)
    private @NotNull String afterMessagePrefix = "REQUEST -> ";
}
