package org.cryptotrader.agent.library.repository

import org.cryptotrader.agent.library.entity.AgentToolExecution
import org.springframework.data.jpa.repository.JpaRepository

interface AgentToolExecutionRepository : JpaRepository<AgentToolExecution, Long>