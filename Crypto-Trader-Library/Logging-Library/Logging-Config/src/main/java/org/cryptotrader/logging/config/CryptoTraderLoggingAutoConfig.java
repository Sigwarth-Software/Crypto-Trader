package org.cryptotrader.logging.config;

import ch.qos.logback.classic.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.cryptotrader.logging.config.aspect.TimeTrackingAspect;
import org.cryptotrader.logging.library.events.publisher.LogEventsPublisher;
import org.cryptotrader.logging.properties.CryptoTraderLoggingProperties;
import org.cryptotrader.universal.library.events.EventPublisher;
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
@EnableConfigurationProperties({CryptoTraderLoggingProperties.class})
public class CryptoTraderLoggingAutoConfig {

    @Bean
    @ConditionalOnMissingBean(name = "globalExceptionHandler")
    @ConditionalOnProperty(prefix = "cryptotrader.exceptions", name = "enabled", havingValue = "true", matchIfMissing = true)
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(StreamBridge.class)
    @ConditionalOnBean(StreamBridge.class)
    public EventPublisher eventPublisher(StreamBridge streamBridge) {
        return new EventPublisher(streamBridge);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogEventsPublisher logEventsPublisher(@Autowired(required = false) EventPublisher eventPublisher) {
        return new LogEventsPublisher(eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public TimeTrackingAspect timeTrackingAspect(@Autowired(required = false) LogEventsPublisher logEventsPublisher) {
        return new TimeTrackingAspect(logEventsPublisher);
    }
}
