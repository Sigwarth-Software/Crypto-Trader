package org.cryptotrader.logging.library.service.entity

import org.cryptotrader.logging.library.entity.FrontendLog
import org.cryptotrader.logging.library.repository.FrontendLogRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FrontendLogEntityService @Autowired constructor(
    repository: FrontendLogRepository
) : BaseEntityService<FrontendLog, Long, FrontendLogRepository>(repository) {

}