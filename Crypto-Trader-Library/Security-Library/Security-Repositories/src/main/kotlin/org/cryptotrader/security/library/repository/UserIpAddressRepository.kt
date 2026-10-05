package org.cryptotrader.security.library.repository

import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.security.library.entity.ip.UserIpAddress
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserIpAddressRepository : JpaRepository<UserIpAddress, Long> {
    fun findByUserAndIpAddress(user: ProductUser, ipAddress: String): Optional<UserIpAddress>
    fun findByUserIdAndIpAddress(userId: Long, ipAddress: String): Optional<UserIpAddress>
    fun findAllByUser(user: ProductUser): List<UserIpAddress>
    fun findAllByUserId(userId: Long): List<UserIpAddress>
    fun findAllByIpAddress(ipAddress: String): List<UserIpAddress>
}
