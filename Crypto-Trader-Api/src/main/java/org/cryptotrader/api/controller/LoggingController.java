package org.cryptotrader.api.controller;

//=================================-Imports-==================================
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.services.AuthContextService;
import org.cryptotrader.logging.library.communication.request.FrontendLogRequest;
import org.cryptotrader.logging.library.communication.response.FrontendLogResponse;
import org.cryptotrader.logging.library.events.FrontendLogBatchEvent;
import org.cryptotrader.logging.library.events.FrontendLogEvent;
import org.cryptotrader.logging.library.events.publisher.LogEventsPublisher;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.cryptotrader.logging.library.scripts.LogMappingScriptKt.mapToEvent;
import static org.cryptotrader.logging.library.scripts.LoggingParsingScriptKt.parseNdjson;

@RestController
@RequestMapping("/api/logs")
@Slf4j
@PermitAll
public class LoggingController {
    //============================-Variables-=================================
    private final @NotNull LogEventsPublisher logEventsPublisher;
    private final @NotNull AuthContextService authContextService;
    //===========================-Constructors-===============================
    @Autowired
    public LoggingController(@NotNull final LogEventsPublisher logEventsPublisher,
                             @NotNull final AuthContextService authContextService) {
        this.logEventsPublisher = logEventsPublisher;
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
    @PostMapping(
        value = "/website",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @NotNull ResponseEntity<FrontendLogResponse> receiveSingleLog(
        @NotNull @RequestBody final FrontendLogRequest logEntry,
        @NotNull final HttpServletRequest request
    ) {
        final boolean isAuthenticated = this.authContextService.isAuthenticated();
        ProductUser user = null;

        if (isAuthenticated) {
            user = this.authContextService.getAuthenticatedProductUser();
        }
        final FrontendLogEvent event = mapToEvent(logEntry, request, user);
        final FrontendLogBatchEvent batch = new FrontendLogBatchEvent(
            List.of(event),
            LocalDateTime.now(ZoneId.of("America/Chicago"))
        );
        this.logEventsPublisher.publishBatch(batch);
        return ResponseEntity.accepted()
            .body(new FrontendLogResponse(1, "accepted"));
    }

    @PostMapping(value = "/website", consumes = "application/x-ndjson")
    public @NotNull ResponseEntity<FrontendLogResponse> receiveBatchLogs(
        @NotNull @RequestBody final String ndjsonBody,
        @NotNull final HttpServletRequest request
    ) {
        final boolean isAuthenticated = this.authContextService.isAuthenticated();
        ProductUser user = null;

        if (isAuthenticated) {
            user = this.authContextService.getAuthenticatedProductUser();
        }
        final List<FrontendLogEvent> entries = parseNdjson(
            ndjsonBody,
            request,
            user
        );
        final FrontendLogBatchEvent batch = new FrontendLogBatchEvent(
            entries,
            LocalDateTime.now(ZoneId.of("America/Chicago"))
        );
        this.logEventsPublisher.publishBatch(batch);
        return ResponseEntity.accepted()
            .body(new FrontendLogResponse(entries.size(), "accepted"));
    }
}
