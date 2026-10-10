package org.cryptotrader.development.library.model.project

enum class GitHubProjectModules(val moduleName: String) {
    CRYPTO_TRADER("Crypto-Trader"),
    CRYPTO_TRADER_ADMIN("Crypto-Trader-Admin"),
    CRYPTO_TRADER_AGENT("Crypto-Trader-Agent"),
    CRYPTO_TRADER_ANALYSIS("Crypto-Trader-Analysis"),
    CRYPTO_TRADER_API("Crypto-Trader-API"),
    CRYPTO_TRADER_ASSETS("Crypto-Trader-Assets"),
    CRYPTO_TRADER_CHAT("Crypto-Trader-Chat"),
    CRYPTO_TRADER_CONSOLE("Crypto-Trader-Console"),
    CRYPTO_TRADER_CONTACT("Crypto-Trader-Contact"),
    CRYPTO_TRADER_COVERAGE("Crypto-Trader-Coverage"),
    CRYPTO_TRADER_DATA("Crypto-Trader-Data"),
    CRYPTO_TRADER_DEVELOPMENT("Crypto-Trader-Development"),
    CRYPTO_TRADER_DOCS("Crypto-Trader-Docs"),
    CRYPTO_TRADER_ENGINE("Crypto-Trader-Engine"),
    CRYPTO_TRADER_HEALTH("Crypto-Trader-Health"),
    CRYPTO_TRADER_LIBRARY("Crypto-Trader-Library"),
    CRYPTO_TRADER_LOGGING("Crypto-Trader-Logging"),
    CRYPTO_TRADER_MOBILE("Crypto-Trader-Mobile"),
    CRYPTO_TRADER_SECURITY("Crypto-Trader-Security"),
    CRYPTO_TRADER_TESTING("Crypto-Trader-Testing"),
    CRYPTO_TRADER_TRANSACTIONS("Crypto-Trader-Transactions"),
    CRYPTO_TRADER_VERSION("Crypto-Trader-Version"),
    CRYPTO_TRADER_WEBSITE("Crypto-Trader-Website");

    companion object {
        fun from(moduleName: String): GitHubProjectModules? {
            return entries.find { it.moduleName.equals(moduleName, ignoreCase = true) }
        }
    }
}