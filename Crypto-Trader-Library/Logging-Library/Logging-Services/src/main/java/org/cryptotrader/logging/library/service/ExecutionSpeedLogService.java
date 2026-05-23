package org.cryptotrader.logging.library.service;

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.logging.library.entity.ExecutionSpeedLog;
import org.cryptotrader.logging.library.events.ExecutionSpeedLogEventPayload;
import org.cryptotrader.logging.library.service.entity.ExecutionSpeedLogEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ExecutionSpeedLogService {

    private final ExecutionSpeedLogEntityService executionSpeedLogEntityService;

    @Autowired
    public ExecutionSpeedLogService(ExecutionSpeedLogEntityService executionSpeedLogEntityService) {
        this.executionSpeedLogEntityService = executionSpeedLogEntityService;
    }

    @Transactional
    public void persist(ExecutionSpeedLogEventPayload entry) {
        ExecutionSpeedLog entity = new ExecutionSpeedLog(
            entry.getExecutionSpeed(),
            entry.getExpectedExecutionSpeed(),
            entry.getFullMethodQualifiedName(),
            entry.getMethodName(),
            entry.getClassName(),
            entry.getTimestamp()
        );
        this.executionSpeedLogEntityService.save(entity);
    }
}
