package org.cryptotrader.admin.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration(value = "adminHttpClientConfig", proxyBeanMethods = false)
public class HttpClientConfig {

    @Bean
    public @NotNull RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
