package org.cryptotrader.assets

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = [
    "org.cryptotrader.assets.library.config"
])
open class CryptoTraderAssetsApplication

fun main(args: Array<String>) {
    runApplication<CryptoTraderAssetsApplication>(*args)
}
