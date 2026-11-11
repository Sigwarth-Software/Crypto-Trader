package org.cryptotrader.agent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Import(McpJacksonConfiguration::class, McpNotFoundExceptionHandler::class)
@SpringBootApplication(
    scanBasePackages = [
        "org.cryptotrader.agent.library.component",
        "org.cryptotrader.agent.library.config",
        "org.cryptotrader.agent.library.infrastructure",
        "org.cryptotrader.universal.library.config",
        "org.cryptotrader.development.library.services",
        "org.cryptotrader.development.library.config",
        "org.cryptotrader.agent.library.services"
    ]
)
@EntityScan(basePackages = ["org.cryptotrader.agent.library.entity"])
@EnableJpaRepositories(basePackages = [
    "org.cryptotrader.agent.library.repository"
])
open class CryptoTraderAgentApplication

fun main(args: Array<String>) {
    runApplication<CryptoTraderAgentApplication>(*args)
}
