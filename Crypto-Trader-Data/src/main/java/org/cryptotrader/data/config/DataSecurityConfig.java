package org.cryptotrader.data.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@Order(0)
public class DataSecurityConfig {

    @Value("${TRUSTED_HOST_IP}")
    private String trustedIp;

    @Bean
    public SecurityFilterChain trustedHostFilterChain(final @NotNull HttpSecurity http) throws Exception {
        http.securityMatcher(isFromTrustedHost())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    private @NotNull RequestMatcher isFromTrustedHost() {
        if (this.trustedIp == null || this.trustedIp.isEmpty()) {
            throw new IllegalStateException("Trusted IP address must be configured");
        }
        return request -> this.trustedIp.equals(request.getRemoteAddr());
    }
}
