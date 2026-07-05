package org.cryptotrader.security.library.service

import org.cryptotrader.security.library.model.BanType
import org.cryptotrader.security.library.service.model.IpBanPolicy
import org.cryptotrader.security.library.service.model.TemporalIpBanManager
import org.springframework.stereotype.Service

@Service
class IpBanService(
    private val tempBanService: InMemoryIpBanService,
    private val permaBanService: IpPermaBanService
) : TemporalIpBanManager {

    override fun isBanned(ip: String): Boolean {
        if (IpBanPolicy.shouldBypass(ip)) return false
        val tempBanned: Boolean = this.tempBanService.isBanned(ip)
        if (tempBanned) {
            return true
        }
        return this.permaBanService.isBanned(ip)
    }

    override fun ban(ipOrCidr: String) {
        this.tempBanService.ban(ipOrCidr)
    }

    override fun unban(ipOrCidr: String) {
        this.tempBanService.unban(ipOrCidr)
    }

    override fun ban(ipOrCidr: String, type: BanType) {
        if (IpBanPolicy.shouldBypass(ipOrCidr)) return
        when (type) {
            BanType.TEMP -> this.tempBanService.ban(ipOrCidr, type)
            BanType.PERMA -> this.permaBanService.ban(ipOrCidr, type)
        }
    }

    override fun unban(ipOrCidr: String, type: BanType) {
        if (IpBanPolicy.shouldBypass(ipOrCidr)) return
        when (type) {
            BanType.TEMP -> this.tempBanService.unban(ipOrCidr, type)
            BanType.PERMA -> this.permaBanService.unban(ipOrCidr, type)
        }
    }

    override fun recordAttempt(ip: String) {
        if (IpBanPolicy.shouldBypass(ip)) return
        this.tempBanService.recordAttempt(ip)
        this.permaBanService.recordAttempt(ip)
    }
}
