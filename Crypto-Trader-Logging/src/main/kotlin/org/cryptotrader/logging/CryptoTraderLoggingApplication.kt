package org.cryptotrader.logging

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["org.cryptotrader.logging.library.repository"])
@EntityScan(basePackages = ["org.cryptotrader.logging.library.entity"])
open class CryptoTraderLoggingApplication

fun main(args: Array<String>) {
    runApplication<CryptoTraderLoggingApplication>(*args)
}