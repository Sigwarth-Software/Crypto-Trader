@file:JvmName("CryptoTraderService")

package org.cryptotrader.health.library.model

enum class CryptoTraderService(val port: Int) {
    ANALYSIS(8000),
    API(8080),
    DATA(8085),
    DATABASE(5432),
    ENGINE(8086),
    CONTACT(8087),
    CONSOLE(8088),
    AGENT(8089),
    CHAT(8090),
    LOGGING(8093),
    HEALTH(8094),
    SECURITY(8095),
    SIMULATOR(8096),
    ADMIN(9000),
    VERSION(9001),
    DOCS(443);

    companion object {
        const val PREFIX = "Crypto Trader"
    }

    override fun toString(): String {
        val normalizedName = this.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$PREFIX $normalizedName".replace(" ", "-")
    }
}
