package org.cryptotrader.security.library.service.model

import org.cryptotrader.security.library.model.BanType

interface TemporalIpBanManager : IpBanManager {
    fun ban(ipOrCidr: String, type: BanType = BanType.TEMP)
    fun unban(ipOrCidr: String, type: BanType = BanType.TEMP)
}