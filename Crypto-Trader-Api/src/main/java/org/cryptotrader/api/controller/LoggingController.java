package org.cryptotrader.api.controller;
//=================================-Imports-==================================
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.services.AuthContextService;
import org.cryptotrader.api.library.services.AuthService;
import org.cryptotrader.logging.library.communication.request.FrontendLogErrorRequest;
import org.cryptotrader.logging.library.communication.request.FrontendLogRequest;
import org.cryptotrader.logging.library.communication.response.FrontendLogResponse;
import org.cryptotrader.logging.library.events.FrontendLogBatchEvent;
import org.cryptotrader.logging.library.events.FrontendLogEvent;
import org.cryptotrader.logging.library.events.publisher.LogEventsPublisher;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@Slf4j
@PermitAll
public class LoggingController {
    //============================-Variables-=================================
    private final LogEventsPublisher logEventsPublisher;
    private final ObjectMapper objectMapper;
    private final AuthContextService authContextService;
    //===========================-Constructors-===============================
    @Autowired
    public LoggingController(LogEventsPublisher logEventsPublisher,
                             ObjectMapper objectMapper,
                             AuthContextService authContextService) {
        this.logEventsPublisher = logEventsPublisher;
        this.objectMapper = objectMapper;
        this.authContextService = authContextService;
    }
    //=============================-Methods-==================================

    // TODO: User should agree to "advanced support" logs for instant support.
    //       If accepted, the logs are saved. If not, they are never sent. By
    //       default, logs are not saved.

    // TODO: Admins can enable temporary "advanced support" while in contact
    //       with the user, while they attempt to replicate the issue. This
    //       would be in Crypto-Trader-Admin where a button with configs can
    //       be pressed to enable for 15/30/60 mins.
    @PostMapping(value = "/website", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FrontendLogResponse> receiveSingleLog(
        @RequestBody FrontendLogRequest logEntry,
        HttpServletRequest request) {
        final boolean isAuthenticated = this.authContextService.isAuthenticated();
        ProductUser user = null;
        if (isAuthenticated) {
            user = this.authContextService.getAuthenticatedProductUser();
        }
        FrontendLogEvent event = this.mapToEvent(logEntry, request, user);
        FrontendLogBatchEvent batch = new FrontendLogBatchEvent(
            List.of(event), LocalDateTime.now(ZoneId.of("America/Chicago")));
        this.logEventsPublisher.publishBatch(batch);
        return ResponseEntity.accepted()
            .body(new FrontendLogResponse(1, "accepted"));
    }

    @PostMapping(value = "/website", consumes = "application/x-ndjson")
    public ResponseEntity<FrontendLogResponse> receiveBatchLogs(
        @RequestBody String ndjsonBody,
        HttpServletRequest request) {
        final boolean isAuthenticated = this.authContextService.isAuthenticated();
        ProductUser user = null;
        if (isAuthenticated) {
            user = this.authContextService.getAuthenticatedProductUser();
        }
        List<FrontendLogEvent> entries = this.parseNdjson(ndjsonBody, request, user);
        FrontendLogBatchEvent batch = new FrontendLogBatchEvent(
            entries, LocalDateTime.now(ZoneId.of("America/Chicago")));
        this.logEventsPublisher.publishBatch(batch);
        return ResponseEntity.accepted()
            .body(new FrontendLogResponse(entries.size(), "accepted"));
    }

    // TODO: Add to service class.
    private FrontendLogEvent mapToEvent(FrontendLogRequest dto, HttpServletRequest request, ProductUser user) {
        FrontendLogErrorRequest error = dto.getError();
        return new FrontendLogEvent(
            dto.getTimestamp(),
            dto.getLevel(),
            dto.getLogger(),
            dto.getContext(),
            dto.getMessage(),
            dto.getMetadata(),
            error != null ? error.getName() : null,
            error != null ? error.getMessage() : null,
            error != null ? error.getStack() : null,
            request.getHeader("x-client-app"),
            request.getHeader("User-Agent"),
            resolveIpAddress(request),
            request.getRemoteAddr(),
            user
        );
    }

    // TODO: Add to service class.
    private String resolveIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // TODO: Add to service class.
    private List<FrontendLogEvent> parseNdjson(String ndjsonBody, HttpServletRequest request, ProductUser user) {
        List<FrontendLogEvent> events = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(ndjsonBody))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    FrontendLogRequest dto = this.objectMapper.readValue(line, FrontendLogRequest.class);
                    events.add(this.mapToEvent(dto, request, user));
                }
            }
        } catch (Exception exception) {
            log.error("Failed to parse NDJSON frontend log batch", exception);
        }
        return events;
    }
}
