package org.cryptotrader.logging.config;

import org.cryptotrader.logging.properties.LogRedactionProperties;
import org.cryptotrader.logging.redaction.LogRedactor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(LogRedactionProperties.class)
public class LogRedactionAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    public @NotNull LogRedactor logRedactor(final LogRedactionProperties properties) {
        return new LogRedactor(properties);
    }
}
