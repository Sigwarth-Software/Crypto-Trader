package org.cryptotrader.security

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@Import(SecurityAutoConfiguration::class)
@EnableJpaRepositories(basePackages = ["org.cryptotrader.security.library.repository"])
@EntityScan(basePackages = [
    "org.cryptotrader.security.library.entity",
    "org.cryptotrader.api.library.entity",
    "org.cryptotrader.data.library.entity"
])
@ComponentScan(basePackages = [
    "org.cryptotrader.security",
    "org.cryptotrader.security.library.service",
    "org.cryptotrader.security.library.event",
    "org.cryptotrader.security.library.config",
    "org.cryptotrader.universal.library.events"
])
open class CryptoTraderSecurityApplication

fun main(args: Array<String>) {
    System.setProperty("cryptotrader.load.currency", "false")
    runApplication<CryptoTraderSecurityApplication>(*args)
}