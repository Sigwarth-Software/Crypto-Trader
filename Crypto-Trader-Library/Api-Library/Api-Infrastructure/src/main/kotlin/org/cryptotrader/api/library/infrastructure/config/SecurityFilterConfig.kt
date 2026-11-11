package org.cryptotrader.api.library.infrastructure.config

import org.cryptotrader.api.library.infrastructure.ClientIpFilter
import org.cryptotrader.api.library.infrastructure.JwtAuthenticationFilter
import org.cryptotrader.api.library.infrastructure.alias.BindingEnforcementFilterBean
import org.cryptotrader.api.library.infrastructure.alias.ClientIpFilterBean
import org.cryptotrader.api.library.infrastructure.alias.DpopValidationFilterBean
import org.cryptotrader.api.library.infrastructure.alias.JwtAuthenticationFilterBean
import org.cryptotrader.api.library.infrastructure.dpop.BindingEnforcementFilter
import org.cryptotrader.api.library.infrastructure.dpop.DpopValidationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Registers the HTTP filter chain for API authentication and tracking.
 *
 * Filter order summary (low number runs first):
 * 10. DpopValidationFilter — Verifies the per‑request DPoP proof, checks replay (jti), and exposes the
 *     computed key thumbprint (jkt) on the request for downstream filters.
 * 20. JwtAuthenticationFilter — Validates the JWT access token (RS256), looks up the user, and places
 *     an authenticated principal in the Spring Security context when valid.
 * 30. BindingEnforcementFilter — Enforces sender‑constrained tokens by comparing token.cnf.jkt with
 *     the DPoP proof jkt captured in step 10. Rejects on mismatch.
 * 40. ClientIpFilter — Captures authenticated user IP address and publishes UserIpDetectionEvent for tracking.
 *
 * All filters are registered for all URL patterns (all paths). This class only wires order; business logic lives
 * inside the individual filter implementations.
 */
@Configuration
open class SecurityFilterConfig {

    /**
     * Register the DPoP validation filter to run before JWT authentication.
     * Order 10 ensures the proof is processed and request attributes are available for later filters.
     */
    @Bean
    open fun dpopValidationFilterRegistration(filter: DpopValidationFilter): DpopValidationFilterBean {
        val registration = FilterRegistrationBean(filter)
        registration.order = 10 // pre-JWT
        registration.addUrlPatterns("/*")
        registration.isEnabled = true
        return registration
    }

    /**
     * Register the JWT authentication filter.
     * Order 20 places it after DPoP validation and before binding enforcement.
     */
    @Bean
    open fun jwtAuthenticationFilterRegistration(filter: JwtAuthenticationFilter): JwtAuthenticationFilterBean {
        val registration = FilterRegistrationBean(filter)
        registration.order = 20 // JWT auth
        registration.addUrlPatterns("/*")
        registration.isEnabled = true
        return registration
    }

    /**
     * Register the binding enforcement filter to run after successful JWT auth.
     * Order 30 ensures it can compare token.cnf.jkt with the DPoP proof jkt.
     */
    @Bean
    open fun bindingEnforcementFilterRegistration(filter: BindingEnforcementFilter): BindingEnforcementFilterBean {
        val registration = FilterRegistrationBean(filter)
        registration.order = 30 // post-JWT
        registration.addUrlPatterns("/*")
        registration.isEnabled = true
        return registration
    }

    /**
     * Register the Client IP detection filter to run after authentication.
     * Order 40 ensures authenticated user context is populated.
     */
    @Bean
    open fun clientIpFilterRegistration(filter: ClientIpFilter): ClientIpFilterBean {
        val registration = FilterRegistrationBean(filter)
        registration.order = 40 // post-auth IP tracking
        registration.addUrlPatterns("/*")
        registration.isEnabled = true
        return registration
    }
}