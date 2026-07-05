package org.cryptotrader.health

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = [
    "org.cryptotrader.health.library.component",
    "org.cryptotrader.health.library.service",
    "org.cryptotrader.health.library.config",
])
@EntityScan(basePackages = ["org.cryptotrader.health.library.entity"])
@EnableJpaRepositories(basePackages = ["org.cryptotrader.health.library.repository"])
open class CryptoTraderHealthApplication

fun main(args: Array<String>) {
    runApplication<CryptoTraderHealthApplication>(*args)
}
