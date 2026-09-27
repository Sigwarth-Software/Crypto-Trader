package org.cryptotrader.logging.library.entity

enum class LogModule(val moduleName: String) {
    CRYPTO_TRADER_ADMIN("crypto-trader-admin"),
    CRYPTO_TRADER_AGENT("crypto-trader-agent"),
    CRYPTO_TRADER_API("crypto-trader-api"),
    CRYPTO_TRADER_CHAT("crypto-trader-chat"),
    CRYPTO_TRADER_CONSOLE("crypto-trader-console"),
    CRYPTO_TRADER_CONTACT("crypto-trader-contact"),
    CRYPTO_TRADER_DATA("crypto-trader-data"),
    CRYPTO_TRADER_ENGINE("crypto-trader-engine"),
    CRYPTO_TRADER_HEALTH("crypto-trader-health"),
    CRYPTO_TRADER_LOGGING("crypto-trader-logging"),
    CRYPTO_TRADER_SECURITY("crypto-trader-security"),
    CRYPTO_TRADER_SIMULATOR("crypto-trader-simulator"),
    CRYPTO_TRADER_VERSION("crypto-trader-version"),
    UNKNOWN("unknown-service");

    companion object {
        @JvmStatic
        fun fromModuleName(moduleName: String?): LogModule =
            entries.firstOrNull { it.moduleName.equals(moduleName, ignoreCase = true) } ?: UNKNOWN
    }
}
