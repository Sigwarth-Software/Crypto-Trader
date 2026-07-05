package org.cryptotrader.logging.library.service.entity

import org.cryptotrader.logging.library.entity.ExecutionSpeedLog
import org.cryptotrader.logging.library.repository.ExecutionSpeedLogRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExecutionSpeedLogEntityService @Autowired constructor(
    repository: ExecutionSpeedLogRepository
) : BaseEntityService<ExecutionSpeedLog, Long, ExecutionSpeedLogRepository>(repository)
