package org.cryptotrader.logging.library.service.entity

import org.cryptotrader.logging.library.entity.ApplicationLog
import org.cryptotrader.logging.library.repository.ApplicationLogRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ApplicationLogEntityService @Autowired constructor(
    repository: ApplicationLogRepository
) : BaseEntityService<ApplicationLog, Long, ApplicationLogRepository>(repository)
