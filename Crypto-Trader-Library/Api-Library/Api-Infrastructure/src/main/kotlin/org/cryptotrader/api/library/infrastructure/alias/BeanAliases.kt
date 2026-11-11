package org.cryptotrader.api.library.infrastructure.alias

import org.cryptotrader.api.library.infrastructure.ClientIpFilter
import org.cryptotrader.api.library.infrastructure.JwtAuthenticationFilter
import org.cryptotrader.api.library.infrastructure.dpop.BindingEnforcementFilter
import org.cryptotrader.api.library.infrastructure.dpop.DpopValidationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean

/**
 * FilterRegistrationBean for DpopValidationFilter (pre-JWT proof verification).
 */
typealias DpopValidationFilterBean = FilterRegistrationBean<DpopValidationFilter>

/**
 * FilterRegistrationBean for JwtAuthenticationFilter (JWT validation and user context population).
 */
typealias JwtAuthenticationFilterBean = FilterRegistrationBean<JwtAuthenticationFilter>

/**
 * FilterRegistrationBean for BindingEnforcementFilter (DPoP cnf.jkt vs proof jkt enforcement, post-JWT).
 */
typealias BindingEnforcementFilterBean = FilterRegistrationBean<BindingEnforcementFilter>

/**
 * FilterRegistrationBean for ClientIpFilter (post-auth IP address tracking).
 */
typealias ClientIpFilterBean = FilterRegistrationBean<ClientIpFilter>