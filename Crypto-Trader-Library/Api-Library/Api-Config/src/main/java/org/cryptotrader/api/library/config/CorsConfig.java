package org.cryptotrader.api.library.config;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Central CORS configuration for the API.
 *
 * Summary:
 * - Reads allowed origins/methods/headers from properties under cryptotrader.api.cors.*
 * - Allows credentials when configured (required for HttpOnly refresh cookie in dev/prod as applicable).
 * - Registers both MVC mapping-based CORS and a CorsConfigurationSource bean for frameworks that use it.
 *
 * Properties:
 * - cryptotrader.api.cors.allowed-origins (default http://localhost:4200)
 * - cryptotrader.api.cors.allowed-methods (default GET,POST,PUT,DELETE,PATCH,OPTIONS)
 * - cryptotrader.api.cors.allowed-headers (default *)
 * - cryptotrader.api.cors.exposed-headers (default Location,Link)
 * - cryptotrader.api.cors.allow-credentials (default true)
 */
@Configuration
@ConditionalOnProperty(name = "cryptotrader.api.cors.enabled", havingValue = "true", matchIfMissing = true)
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cryptotrader.api.cors.allowed-origins:http://localhost:4200}")
    private List<String> allowedOrigins;

    @Value("${cryptotrader.api.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private List<String> allowedMethods;

    @Value("${cryptotrader.api.cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    @Value("${cryptotrader.api.cors.exposed-headers:Location,Link}")
    private List<String> exposedHeaders;

    @Value("${cryptotrader.api.cors.allow-credentials:true}")
    private boolean allowCredentials;

    /**
     * Programmatically registers CORS rules for all controller endpoints.
     * Uses properties to set allowed origins/methods/headers and whether credentials are allowed.
     */
    @Override
    public void addCorsMappings(final @NotNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(this.allowedOrigins.toArray(String[]::new))
                .allowedMethods(this.allowedMethods.toArray(String[]::new))
                .allowedHeaders(this.allowedHeaders.toArray(String[]::new))
                .allowCredentials(this.allowCredentials)
                .exposedHeaders(this.exposedHeaders.toArray(String[]::new));
    }

    /**
     * Provide a CorsConfigurationSource bean for components that look it up directly (e.g. Spring Security).
     * Mirrors the same properties as addCorsMappings for consistency.
     */
    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource.class)
    public @NotNull CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(this.allowedOrigins);
        config.setAllowedMethods(this.allowedMethods);
        config.setAllowedHeaders(this.allowedHeaders);
        config.setAllowCredentials(this.allowCredentials);
        config.setExposedHeaders(this.exposedHeaders);
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
