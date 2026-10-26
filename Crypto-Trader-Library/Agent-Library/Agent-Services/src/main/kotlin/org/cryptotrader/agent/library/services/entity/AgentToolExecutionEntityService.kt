package org.cryptotrader.agent.library.services.entity

import org.cryptotrader.agent.library.entity.AgentToolExecution
import org.cryptotrader.agent.library.repository.AgentToolExecutionRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AgentToolExecutionEntityService @Autowired constructor(
    repository: AgentToolExecutionRepository
) : BaseEntityService<AgentToolExecution, Long, AgentToolExecutionRepository>(
    repository
)