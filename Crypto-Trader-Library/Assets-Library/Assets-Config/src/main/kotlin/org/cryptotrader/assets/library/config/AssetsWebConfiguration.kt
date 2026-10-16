package org.cryptotrader.assets.library.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

// TODO: Split into two files.

@Configuration
open class AssetsWebConfiguration : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/assets/")
            .setCachePeriod(3600)
    }
}

@Configuration
open class AssetsSecurityConfiguration {

    @Bean
    open fun assetsSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { authorize ->
            authorize.requestMatchers("/assets/**", "/actuator/health", "/actuator/health/**").permitAll()
            authorize.anyRequest().authenticated()
        }
        http.httpBasic(Customizer.withDefaults())
        http.formLogin(Customizer.withDefaults())
        return http.build()
    }
}
