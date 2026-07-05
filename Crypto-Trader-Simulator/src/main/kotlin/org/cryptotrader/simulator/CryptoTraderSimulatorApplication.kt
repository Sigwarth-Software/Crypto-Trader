package org.cryptotrader.simulator

import org.cryptotrader.universal.library.component.SystemScripts
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = [
    "org.cryptotrader.simulator.library.config",
    "org.cryptotrader.simulator.library.services",
    "org.cryptotrader.data.library.services",
    "org.cryptotrader.data.library.component",
    "org.cryptotrader.simulator.library.events",
    "org.cryptotrader.data.library.repository",
    "org.cryptotrader.universal.library.events",
    "org.cryptotrader.universal.library.config"
])
@EnableJpaRepositories(basePackages = ["org.cryptotrader.data.library.repository"])
@EntityScan(basePackages = ["org.cryptotrader.data.library.entity"])
class CryptoTraderSimulatorApplication

fun main(args: Array<String>) {
    SystemScripts.blockCurrencyLoading()
    SystemScripts.blockCurrencyHarvesting()
    runApplication<CryptoTraderSimulatorApplication>(*args)
}