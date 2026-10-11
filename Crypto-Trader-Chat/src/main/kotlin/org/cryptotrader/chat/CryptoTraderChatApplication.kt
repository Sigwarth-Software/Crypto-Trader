package org.cryptotrader.chat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = [
    "org.cryptotrader.chat.library.config",
])
class CryptoTraderChatApplication

fun main(args: Array<String>) {
    runApplication<CryptoTraderChatApplication>(*args)
}
