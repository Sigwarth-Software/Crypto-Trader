package org.cryptotrader.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "cryptotrader.api.jpa.enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaRepositories(
    basePackages = {
        "org.cryptotrader.api.library.repository",
        "org.cryptotrader.data.library.repository",
        "org.cryptotrader.security.library.repository"
    }
)
public class JpaRepositoriesConfig {

}
