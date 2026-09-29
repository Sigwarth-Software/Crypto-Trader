package org.cryptotrader.logging.library.service;

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.logging.library.entity.ApplicationLog;
import org.cryptotrader.logging.library.entity.LogLevel;
import org.cryptotrader.logging.library.entity.LogModule;
import org.cryptotrader.logging.library.events.ApplicationLogEventPayload;
import org.cryptotrader.logging.library.service.entity.ApplicationLogEntityService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class ApplicationLogService {
    private final ObjectMapper objectMapper;
    private final ApplicationLogEntityService applicationLogEntityService;

    @Autowired
    public ApplicationLogService(final ObjectMapper objectMapper, final ApplicationLogEntityService applicationLogEntityService) {
        this.objectMapper = objectMapper;
        this.applicationLogEntityService = applicationLogEntityService;
    }

    @Transactional
    public void persist(final @NotNull ApplicationLogEventPayload entry, final LocalDateTime receivedAt) {
        final ApplicationLog entity = ApplicationLog.builder()
                .timestamp(entry.getTimestamp())
                .level(LogLevel.fromLevelName(entry.getLevel()))
                .logger(entry.getLogger())
                .module(LogModule.fromModuleName(entry.getModule()))
                .threadName(entry.getThreadName())
                .message(entry.getMessage())
                .metadata(serializeMetadata(entry.getMdcContext()))
                .errorName(entry.getErrorName())
                .errorMessage(entry.getErrorMessage())
                .errorStack(entry.getErrorStack())
                .receivedAt(receivedAt)
                .build();
        this.applicationLogEntityService.save(entity);
    }

    private String serializeMetadata(final @Nullable Map<String, String> metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (final JsonProcessingException jsonProcessingException) {
            log.warn("Failed to serialize metadata, falling back to toString()", jsonProcessingException);
            return "{}";
        }
    }
}
