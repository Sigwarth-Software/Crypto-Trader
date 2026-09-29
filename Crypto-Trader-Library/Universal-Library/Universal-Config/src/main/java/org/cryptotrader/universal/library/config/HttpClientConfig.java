package org.cryptotrader.universal.library.config;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class HttpClientConfig {
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public @NotNull RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(CloseableHttpClient.class)
    public CloseableHttpClient httpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    @ConditionalOnMissingBean(HttpGet.class)
    public @NotNull HttpGet getHttpClient() {
        return new HttpGet();
    }

    @Bean
    @ConditionalOnMissingBean(HttpPost.class)
    public @NotNull HttpPost postHttpClient() {
        return new HttpPost();
    }

    @Bean
    @ConditionalOnMissingBean(HttpPut.class)
    public @NotNull HttpPut putHttpClient() {
        return new HttpPut();
    }

    @Bean
    @ConditionalOnMissingBean(HttpDelete.class)
    public @NotNull HttpDelete deleteHttpClient() {
        return new HttpDelete();
    }

    @Bean
    @ConditionalOnMissingBean(HttpOptions.class)
    public @NotNull HttpOptions optionsHttpClient() {
        return new HttpOptions();
    }

    @Bean
    @ConditionalOnMissingBean(HttpClient.class)
    public HttpClient javaHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
}
