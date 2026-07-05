package org.cryptotrader.api.config;

import org.cryptotrader.security.library.repository.keyset.TinkKeysetRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "cryptotrader.api.jpa.enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaRepositories(
    basePackages = {
        "org.cryptotrader.api.library.repository",
        "org.cryptotrader.data.library.repository",
        "org.cryptotrader.security.library.repository"
    },
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = TinkKeysetRepository.class
        )
    }
)
public class JpaRepositoriesConfig { }
