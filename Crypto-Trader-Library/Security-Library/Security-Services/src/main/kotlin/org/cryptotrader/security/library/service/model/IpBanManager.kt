package org.cryptotrader.security.library.service.model

interface IpBanManager {
    fun isBanned(ip: String): Boolean
    fun ban(ipOrCidr: String)
    fun unban(ipOrCidr: String)
    fun recordAttempt(ip: String)
}
