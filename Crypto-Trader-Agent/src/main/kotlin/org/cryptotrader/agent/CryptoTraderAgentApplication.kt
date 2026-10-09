package org.cryptotrader.agent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(McpJacksonConfiguration::class, McpNotFoundExceptionHandler::class)
@SpringBootApplication(
    scanBasePackages = [
        "org.cryptotrader.agent.library.component",
        "org.cryptotrader.agent.library.config",
        "org.cryptotrader.universal.library.config",
        "org.cryptotrader.development.library.services",
        "org.cryptotrader.development.library.config"
    ]
)
open class CryptoTraderAgentApplication

fun main(args: Array<String>) {
    runApplication<CryptoTraderAgentApplication>(*args)
}
