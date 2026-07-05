package org.cryptotrader.health.library.service.entity

import org.cryptotrader.health.library.entity.HealthStatus
import org.cryptotrader.health.library.repository.HealthStatusRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class HealthStatusEntityService @Autowired constructor(
    healthStatusRepository: HealthStatusRepository
) : BaseEntityService<HealthStatus, Long, HealthStatusRepository>(healthStatusRepository) {

}