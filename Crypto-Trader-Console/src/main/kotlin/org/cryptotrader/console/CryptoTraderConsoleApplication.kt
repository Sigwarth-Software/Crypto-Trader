package org.cryptotrader.console

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = [
    "org.cryptotrader.console.library.component",
    "org.cryptotrader.console.library.services",
    "org.cryptotrader.console.library.model",
    "org.cryptotrader.console.library.entity",
    "org.cryptotrader.console.library.communication",
    "org.cryptotrader.console.library.events",
    "org.cryptotrader.console.library.repository",
    "org.cryptotrader.api.library.component",
    "org.cryptotrader.api.library.services",
    "org.cryptotrader.data.library.services",
    "org.cryptotrader.data.library.component",
    "org.cryptotrader.universal.library.events",
    "org.cryptotrader.api.library.events"
])
@EnableJpaRepositories(basePackages = [
    "org.cryptotrader.console.library.repository",
    "org.cryptotrader.data.library.repository",
    "org.cryptotrader.api.library.repository"
])
@EntityScan(basePackages = [
    "org.cryptotrader.console.library.entity",
    "org.cryptotrader.data.library.entity",
    "org.cryptotrader.api.library.entity"
])
open class CryptoTraderConsoleApplication

fun main(args: Array<String>) {
    System.setProperty("cryptotrader.load.currency", "false")
    runApplication<CryptoTraderConsoleApplication>(*args)
}
