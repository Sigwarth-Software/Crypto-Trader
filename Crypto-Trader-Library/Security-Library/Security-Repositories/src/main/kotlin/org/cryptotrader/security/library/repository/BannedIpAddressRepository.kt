package org.cryptotrader.security.library.repository

import org.cryptotrader.security.library.entity.ip.BannedIpAddress
import org.springframework.data.jpa.repository.JpaRepository

interface BannedIpAddressRepository : JpaRepository<BannedIpAddress, Long>