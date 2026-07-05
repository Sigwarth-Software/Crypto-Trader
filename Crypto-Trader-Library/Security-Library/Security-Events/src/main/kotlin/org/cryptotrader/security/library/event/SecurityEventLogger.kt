package org.cryptotrader.security.library.event

import org.cryptotrader.security.library.entity.ip.BannedIpAddress
import org.cryptotrader.security.library.service.entity.BannedIpAddressEntityService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener

class SecurityEventLogger(
    private val ipAddressEntityService: BannedIpAddressEntityService,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun recordIpEvent(ipAddress: String) {
        try {
            val record = BannedIpAddress(ipAddress)
            this.ipAddressEntityService.save(record)
        } catch (ex: Exception) {
            this.log.warn("Failed to persist security event: {}", ex.toString())
        }
    }
}
