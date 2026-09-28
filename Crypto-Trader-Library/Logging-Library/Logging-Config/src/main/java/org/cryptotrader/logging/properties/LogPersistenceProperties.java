package org.cryptotrader.logging.properties;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cryptotrader.logging.persistence")
public class LogPersistenceProperties {
    private @NotNull LogPersistenceMode mode = LogPersistenceMode.DATABASE;
}
