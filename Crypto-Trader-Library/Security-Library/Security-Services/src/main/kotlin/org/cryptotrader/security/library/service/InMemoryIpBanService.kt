package org.cryptotrader.security.library.service

import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import org.cryptotrader.security.library.model.BanType
import org.cryptotrader.security.library.service.model.IpBanPolicy
import org.cryptotrader.security.library.service.model.TemporalIpBanManager
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class InMemoryIpBanService : TemporalIpBanManager {
    private val _blockedAddresses: MutableMap<String, IPAddressString> =
        ConcurrentHashMap()
    val blockedAddresses: MutableMap<String, IPAddressString>
        get() = this._blockedAddresses

    override fun isBanned(ip: String): Boolean {
        if (IpBanPolicy.shouldBypass(ip)) {
            return false
        }
        val clientAddress: IPAddress = this.parseAddress(ip) ?: return false
        if (this.blockedAddresses.containsKey(ip)) return true
        for (blocked in this.blockedAddresses.values) {
            val blockedAddress: IPAddress = blocked.address ?: continue
            if (blockedAddress.contains(clientAddress) || blockedAddress == clientAddress) {
                return true
            }
        }
        return false
    }

    override fun ban(ipOrCidr: String) {
        this.ban(ipOrCidr, BanType.TEMP)
    }

    override fun unban(ipOrCidr: String) {
        this.blockedAddresses.remove(ipOrCidr)
    }

    private fun parseAddress(value: String): IPAddress? = IPAddressString(
        value
    ).address

    override fun ban(
        ipOrCidr: String,
        type: BanType
    ) {
        if (IpBanPolicy.shouldBypass(ipOrCidr)) return
        if (type == BanType.PERMA) return
        this.blockedAddresses[ipOrCidr] = IPAddressString(ipOrCidr)
    }

    override fun unban(
        ipOrCidr: String,
        type: BanType
    ) {
        if (IpBanPolicy.shouldBypass(ipOrCidr)) return
        if (type == BanType.PERMA) return
        this.blockedAddresses.remove(ipOrCidr)
    }

    override fun recordAttempt(ip: String) {
        // TODO: Implement this.
        return
    }
}
