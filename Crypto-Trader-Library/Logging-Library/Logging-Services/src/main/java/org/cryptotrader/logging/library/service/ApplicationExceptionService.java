package org.cryptotrader.logging.library.service;

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.logging.library.entity.ApplicationException;
import org.cryptotrader.logging.library.entity.LogLevel;
import org.cryptotrader.logging.library.entity.LogModule;
import org.cryptotrader.logging.library.events.ApplicationExceptionEventPayload;
import org.cryptotrader.logging.library.service.entity.ApplicationExceptionEntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ApplicationExceptionService {
    private final ApplicationExceptionEntityService applicationExceptionEntityService;

    @Autowired
    public ApplicationExceptionService(final ApplicationExceptionEntityService applicationExceptionEntityService) {
        this.applicationExceptionEntityService = applicationExceptionEntityService;
    }

    @Transactional
    public void persist(final @NotNull ApplicationExceptionEventPayload entry, final LocalDateTime receivedAt) {
        final ApplicationException entity = ApplicationException.builder()
                .timestamp(entry.getTimestamp())
                .module(LogModule.fromModuleName(entry.getModule()))
                .logger(entry.getLogger())
                .level(LogLevel.fromLevelName(entry.getLevel()))
                .threadName(entry.getThreadName())
                .exceptionClass(entry.getExceptionClass())
                .exceptionMessage(entry.getExceptionMessage())
                .stackTrace(entry.getStackTrace())
                .rootCauseClass(entry.getRootCauseClass())
                .rootCauseMessage(entry.getRootCauseMessage())
                .receivedAt(receivedAt)
                .build();
        this.applicationExceptionEntityService.save(entity);
    }
}
