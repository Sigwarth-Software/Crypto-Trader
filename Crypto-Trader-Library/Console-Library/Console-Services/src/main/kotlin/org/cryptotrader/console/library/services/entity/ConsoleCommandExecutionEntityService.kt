package org.cryptotrader.console.library.services.entity

import org.cryptotrader.console.library.entity.ConsoleCommandExecution
import org.cryptotrader.console.library.repository.ConsoleCommandExecutionRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ConsoleCommandExecutionEntityService @Autowired constructor(
    repository: ConsoleCommandExecutionRepository
) : BaseEntityService<ConsoleCommandExecution, Long, ConsoleCommandExecutionRepository>(repository) {

}