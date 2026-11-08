package org.cryptotrader.health.library.service.entity

import org.cryptotrader.health.library.entity.HealthStatus
import org.cryptotrader.health.library.model.CryptoTraderService
import org.cryptotrader.health.library.repository.HealthStatusRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HealthStatusEntityService @Autowired constructor(
    val healthStatusRepository: HealthStatusRepository
) : BaseEntityService<HealthStatus, Long, HealthStatusRepository>(healthStatusRepository) {
    fun getBetweenTimestamps(
        service: CryptoTraderService,
        startTimeStamp: LocalDateTime,
        endTimeStamp: LocalDateTime
    ): List<HealthStatus> {
        return this.healthStatusRepository.findByServiceAndCheckedAtBetween(service, startTimeStamp, endTimeStamp)
    }

    fun getLatestByService(service: CryptoTraderService): HealthStatus? {
        return this.healthStatusRepository.findFirstByServiceOrderByCheckedAtDesc(service)
    }
}