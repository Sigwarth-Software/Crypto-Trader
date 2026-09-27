package org.cryptotrader.logging.library.service.entity

import org.cryptotrader.logging.library.entity.ApplicationException
import org.cryptotrader.logging.library.repository.ApplicationExceptionRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ApplicationExceptionEntityService @Autowired constructor(
    repository: ApplicationExceptionRepository
) : BaseEntityService<ApplicationException, Long, ApplicationExceptionRepository>(repository)
