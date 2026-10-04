package org.cryptotrader.logging.config;

import ch.qos.logback.classic.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.cryptotrader.logging.config.aspect.TimeTrackingAspect;
import org.cryptotrader.logging.library.events.publisher.LogEventsPublisher;
import org.cryptotrader.logging.properties.CryptoTraderLoggingProperties;
import org.cryptotrader.logging.properties.LogPersistenceProperties;
import org.cryptotrader.logging.properties.TimeTrackingProperties;
import org.cryptotrader.logging.redaction.LogRedactor;
import org.cryptotrader.universal.library.events.EventPublisher;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({Aspect.class, ProceedingJoinPoint.class, Logger.class})
@EnableConfigurationProperties({CryptoTraderLoggingProperties.class, TimeTrackingProperties.class, LogPersistenceProperties.class})
public class CryptoTraderLoggingAutoConfig {

    @Bean
    @ConditionalOnMissingBean(name = "globalExceptionHandler")
    @ConditionalOnProperty(prefix = "cryptotrader.exceptions", name = "enabled", havingValue = "true", matchIfMissing = true)
    public @NotNull GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(StreamBridge.class)
    @ConditionalOnBean(StreamBridge.class)
    public @NotNull EventPublisher eventPublisher(final StreamBridge streamBridge) {
        return new EventPublisher(streamBridge);
    }

    @Bean
    @ConditionalOnMissingBean
    public @NotNull LogEventsPublisher logEventsPublisher(@Autowired(required = false) final EventPublisher eventPublisher) {
        return new LogEventsPublisher(eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public @NotNull TimeTrackingAspect timeTrackingAspect(@Autowired(required = false) final LogEventsPublisher logEventsPublisher,
                                                          final TimeTrackingProperties timeTrackingProperties) {
        return new TimeTrackingAspect(logEventsPublisher, timeTrackingProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public @NotNull LogEventPublisherBridge logEventPublisherBridge(final LogEventsPublisher logEventsPublisher,
                                                                    @Autowired(required = false) final LogRedactor logRedactor) {
        return new LogEventPublisherBridge(logEventsPublisher, logRedactor);
    }
}
