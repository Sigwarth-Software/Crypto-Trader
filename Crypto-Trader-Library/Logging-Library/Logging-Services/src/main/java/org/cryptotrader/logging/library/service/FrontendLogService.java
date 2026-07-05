package org.cryptotrader.logging.library.service;

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.logging.library.service.entity.FrontendLogEntityService;
import org.cryptotrader.logging.library.entity.FrontendLog;
import org.cryptotrader.logging.library.events.FrontendLogEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FrontendLogService {

    private final ObjectMapper objectMapper;
    private final FrontendLogEntityService frontendLogEntityService;

    @Autowired
    public FrontendLogService(ObjectMapper objectMapper, FrontendLogEntityService frontendLogEntityService) {
        this.objectMapper = objectMapper;
        this.frontendLogEntityService = frontendLogEntityService;
    }

    @Transactional
    public void persist(List<FrontendLogEvent> entries, LocalDateTime receivedAt) {
        List<FrontendLog> entities = entries.stream()
                .map(entry -> FrontendLog.builder()
                        .timestamp(entry.getTimestamp())
                        .level(entry.getLevel())
                        .logger(entry.getLogger())
                        .context(entry.getContext())
                        .message(entry.getMessage())
                        .metadata(serializeMetadata(entry.getMetadata()))
                        .errorName(entry.getErrorName())
                        .errorMessage(entry.getErrorMessage())
                        .errorStack(entry.getErrorStack())
                        .clientApp(entry.getClientApp())
                        .userAgent(entry.getUserAgent())
                        .ipAddress(entry.getIpAddress())
                        .remoteAddress(entry.getRemoteAddress())
                        .receivedAt(receivedAt)
                        .build())
                .toList();

        if (entities.isEmpty()) {
            return;
        }

        if (entities.size() == 1) {
            this.frontendLogEntityService.save(entities.getFirst());
            return;
        }
        this.frontendLogEntityService.saveAll(entities);
        log.info("Persisted {} frontend log entries", entities.size());
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException jsonProcessingException) {
            log.warn("Failed to serialize metadata, falling back to toString()", jsonProcessingException);
            return "{}";
        }
    }
}
