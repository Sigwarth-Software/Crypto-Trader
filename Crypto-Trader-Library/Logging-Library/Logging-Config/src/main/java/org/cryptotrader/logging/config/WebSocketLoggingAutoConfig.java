package org.cryptotrader.logging.config;

import org.cryptotrader.logging.properties.CryptoTraderWebSocketLoggingProperties;
import org.cryptotrader.logging.redaction.LogRedactor;
import org.cryptotrader.logging.websocket.StompChannelLoggingInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@AutoConfiguration
@ConditionalOnClass(WebSocketMessageBrokerConfigurer.class)
@ConditionalOnProperty(prefix = "cryptotrader.websocket.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketLoggingAutoConfig implements WebSocketMessageBrokerConfigurer {

    private final ObjectProvider<StompChannelLoggingInterceptor> interceptorProvider;

    @Autowired
    public WebSocketLoggingAutoConfig(ObjectProvider<StompChannelLoggingInterceptor> interceptorProvider) {
        this.interceptorProvider = interceptorProvider;
    }

    @Bean
    @ConditionalOnMissingBean
    public CryptoTraderWebSocketLoggingProperties cryptoTraderWebSocketLoggingProperties() {
        return new CryptoTraderWebSocketLoggingProperties();
    }

    @Bean
    @ConditionalOnMissingBean(StompChannelLoggingInterceptor.class)
    public StompChannelLoggingInterceptor stompChannelLoggingInterceptor(CryptoTraderWebSocketLoggingProperties props,
                                                                         LogRedactor logRedactor) {
        return new StompChannelLoggingInterceptor(props, logRedactor);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        StompChannelLoggingInterceptor interceptor = interceptorProvider.getIfAvailable();
        if (interceptor != null) {
            registration.interceptors(interceptor);
        }
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        StompChannelLoggingInterceptor interceptor = interceptorProvider.getIfAvailable();
        if (interceptor != null) {
            registration.interceptors(interceptor);
        }
    }
}
