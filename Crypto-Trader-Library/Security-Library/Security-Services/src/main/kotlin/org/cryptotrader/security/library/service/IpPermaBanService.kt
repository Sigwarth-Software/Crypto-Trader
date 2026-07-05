package org.cryptotrader.security.library.service

import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import org.cryptotrader.security.library.entity.ip.BannedIpAddress
import org.cryptotrader.security.library.model.BanType
import org.cryptotrader.security.library.service.entity.BannedIpAddressEntityService
import org.cryptotrader.security.library.service.model.IpBanPolicy
import org.cryptotrader.security.library.service.model.TemporalIpBanManager
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class IpPermaBanService(
    private val ipAddressEntityService: BannedIpAddressEntityService,
) : TemporalIpBanManager {

    override fun isBanned(ip: String): Boolean {
        if (IpBanPolicy.shouldBypass(ip)) return false
        val clientAddress: IPAddress = this.parseAddress(ip) ?: return false
        return this.ipAddressEntityService.findAll().any { stored ->
            this.matchesStoredBan(stored.ipAddress, clientAddress)
        }
    }

    override fun ban(ipOrCidr: String) {
        this.ban(ipOrCidr, BanType.PERMA)
    }

    override fun unban(ipOrCidr: String) {
        this.unban(ipOrCidr, BanType.PERMA)
    }

    override fun ban(ipOrCidr: String, type: BanType) {
        if (IpBanPolicy.shouldBypass(ipOrCidr)) return
        if (type != BanType.PERMA) return
        if (this.ipAddressEntityService.findAll().any { stored -> stored.ipAddress == ipOrCidr }) {
            return
        }
        val record = BannedIpAddress()
        record.ipAddress = ipOrCidr
        record.occurredAt = LocalDateTime.now()
        this.ipAddressEntityService.save(record)
    }

    override fun unban(ipOrCidr: String, type: BanType) {
        if (IpBanPolicy.shouldBypass(ipOrCidr)) return
        if (type != BanType.PERMA) return
        this.ipAddressEntityService.findAll()
            .filter { stored -> stored.ipAddress == ipOrCidr }
            .forEach { stored -> this.ipAddressEntityService.delete(stored) }
    }

    override fun recordAttempt(ip: String) {
        if (IpBanPolicy.shouldBypass(ip)) return
        val clientAddress: IPAddress = this.parseAddress(ip) ?: return
        val matchingBan: BannedIpAddress = this.ipAddressEntityService.findAll()
            .firstOrNull { stored ->
                this.matchesStoredBan(stored.ipAddress, clientAddress)
            }
            ?: return

        matchingBan.attempts += 1
        this.ipAddressEntityService.save(matchingBan)
    }

    private fun matchesStoredBan(storedBan: String, clientAddress: IPAddress): Boolean {
        val blockedAddress: IPAddress = this.parseAddress(storedBan) ?: return false
        return blockedAddress.contains(clientAddress) || blockedAddress == clientAddress
    }

    private fun parseAddress(value: String): IPAddress? = IPAddressString(value).address
}
