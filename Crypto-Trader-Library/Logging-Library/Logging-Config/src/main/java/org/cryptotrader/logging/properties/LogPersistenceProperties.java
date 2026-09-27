package org.cryptotrader.logging.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cryptotrader.logging.persistence")
public class LogPersistenceProperties {
    private LogPersistenceMode mode = LogPersistenceMode.DATABASE;
}
