package org.cryptotrader.logging.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "cryptotrader.logging.redaction")
public class LogRedactionProperties {
    private boolean enabled = true;
    private @NotNull String replacement = "[REDACTED]";
    private @NotNull Set<String> fields = new LinkedHashSet<>(Set.of(
            "password",
            "confirmPassword",
            "currentPassword",
            "newPassword",
            "token",
            "accessToken",
            "refreshToken",
            "secret",
            "apiKey",
            "privateKey"
    ));
    private @NotNull Set<String> headers = new LinkedHashSet<>(Set.of(
            "authorization",
            "cookie",
            "set-cookie",
            "x-api-key"
    ));
}
