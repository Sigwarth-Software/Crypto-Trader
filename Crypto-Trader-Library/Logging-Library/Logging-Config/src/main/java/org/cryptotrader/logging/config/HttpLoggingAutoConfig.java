package org.cryptotrader.logging.config;

import org.cryptotrader.logging.properties.CryptoTraderHttpLoggingProperties;
import org.cryptotrader.logging.http.HttpExchangeLoggingFilter;
import org.cryptotrader.logging.redaction.LogRedactor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.util.ContentCachingRequestWrapper;

@AutoConfiguration
@ConditionalOnClass(ContentCachingRequestWrapper.class)
@ConditionalOnProperty(prefix = "cryptotrader.http.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HttpLoggingAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    public HttpExchangeLoggingFilter httpExchangeLoggingFilter(CryptoTraderHttpLoggingProperties props,
                                                               LogRedactor logRedactor) {
        return new HttpExchangeLoggingFilter(
                props.isIncludeQueryString(),
                props.isIncludePayload(),
                props.getMaxPayloadLength(),
                props.isIncludeHeaders(),
                props.isIncludeResponsePayload(),
                props.getMaxResponsePayloadLength(),
                props.isColorEnabled(),
                logRedactor
        );
    }

    @Bean
    public CryptoTraderHttpLoggingProperties cryptoTraderHttpLoggingProperties() {
        return new CryptoTraderHttpLoggingProperties();
    }
}
