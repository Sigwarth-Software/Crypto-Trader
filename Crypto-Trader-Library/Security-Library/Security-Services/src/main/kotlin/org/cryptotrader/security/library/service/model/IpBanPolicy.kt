package org.cryptotrader.security.library.service.model

// TODO: Move to Security-Scripts.
object IpBanPolicy {
    private val ignoredAddresses = setOf(
        "localhost",
        "127.0.0.1",
        "::1",
        "0:0:0:0:0:0:0:1",
        "::ffff:127.0.0.1"
    )

    fun shouldBypass(ip: String): Boolean {
        val normalized: String = ip.trim().lowercase()
        return normalized.isNotEmpty() && normalized in this.ignoredAddresses
    }
}
