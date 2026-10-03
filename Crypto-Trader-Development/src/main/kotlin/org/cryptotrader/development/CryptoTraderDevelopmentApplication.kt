package org.cryptotrader.development

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class CryptoTraderDevelopmentApplication

fun main(args: Array<String>) {
    runApplication<CryptoTraderDevelopmentApplication>(*args)
}