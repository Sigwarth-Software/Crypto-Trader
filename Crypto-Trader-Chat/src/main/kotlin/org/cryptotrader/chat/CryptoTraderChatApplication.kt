package org.cryptotrader.chat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = [
    "org.cryptotrader.chat",
    "org.cryptotrader.chat.library.config",
    "org.cryptotrader.chat.library.services",
    "org.cryptotrader.universal.library.events",
    "org.cryptotrader.universal.library.config",
])
@EntityScan(basePackages = [
    "org.cryptotrader.chat.library.entity",
    "org.cryptotrader.api.library.entity",
    "org.cryptotrader.data.library.entity"
])
@EnableJpaRepositories(
    basePackages = [
        "org.cryptotrader.chat.library.repository"
    ]
)
class CryptoTraderChatApplication

fun main(args: Array<String>) {
    runApplication<CryptoTraderChatApplication>(*args)
}
