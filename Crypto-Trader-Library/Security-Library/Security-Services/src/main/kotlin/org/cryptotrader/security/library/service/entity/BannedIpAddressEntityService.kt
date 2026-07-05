package org.cryptotrader.security.library.service.entity

import org.cryptotrader.security.library.entity.ip.BannedIpAddress
import org.cryptotrader.security.library.repository.BannedIpAddressRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BannedIpAddressEntityService @Autowired constructor(
    repository: BannedIpAddressRepository
) : BaseEntityService<BannedIpAddress, Long, BannedIpAddressRepository>(repository) {

}