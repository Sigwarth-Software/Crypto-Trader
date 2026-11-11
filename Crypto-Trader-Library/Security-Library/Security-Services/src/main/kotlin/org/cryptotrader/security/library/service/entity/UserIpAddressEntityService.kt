package org.cryptotrader.security.library.service.entity

import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.security.library.entity.ip.UserIpAddress
import org.cryptotrader.security.library.repository.UserIpAddressRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Optional

@Service
open class UserIpAddressEntityService @Autowired constructor(
    repository: UserIpAddressRepository
) : BaseEntityService<UserIpAddress, Long, UserIpAddressRepository>(repository) {

    open fun findByUserAndIpAddress(user: ProductUser, ipAddress: String): Optional<UserIpAddress> {
        return this.repository.findByUserAndIpAddress(user, ipAddress)
    }

    open fun findByUserIdAndIpAddress(userId: Long, ipAddress: String): Optional<UserIpAddress> {
        return this.repository.findByUserIdAndIpAddress(userId, ipAddress)
    }

    open fun findAllByUser(user: ProductUser): List<UserIpAddress> {
        return this.repository.findAllByUser(user)
    }

    open fun findAllByUserId(userId: Long): List<UserIpAddress> {
        return this.repository.findAllByUserId(userId)
    }

    open fun findAllByIpAddress(ipAddress: String): List<UserIpAddress> {
        return this.repository.findAllByIpAddress(ipAddress)
    }

    @Transactional
    open fun recordUserIp(user: ProductUser, ipAddress: String): UserIpAddress {
        val existing: Optional<UserIpAddress> = this.findByUserAndIpAddress(user, ipAddress)
        return if (existing.isPresent) {
            val userIp: UserIpAddress = existing.get()
            userIp.lastAccessed = LocalDateTime.now()
            this.save(userIp)
        } else {
            val newIp = UserIpAddress(user, ipAddress, LocalDateTime.now())
            this.save(newIp)
        }
    }
}
