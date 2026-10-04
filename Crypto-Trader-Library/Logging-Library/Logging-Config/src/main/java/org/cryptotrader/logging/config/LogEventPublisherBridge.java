package org.cryptotrader.logging.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.cryptotrader.logging.library.events.publisher.LogEventsPublisher;
import org.cryptotrader.logging.redaction.LogRedactor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicReference;

public class LogEventPublisherBridge {
    private static final AtomicReference<LogEventsPublisher> PUBLISHER = new AtomicReference<>();
    private static final AtomicReference<LogRedactor> REDACTOR = new AtomicReference<>();

    private final LogEventsPublisher logEventsPublisher;
    private final LogRedactor logRedactor;

    public LogEventPublisherBridge(final LogEventsPublisher logEventsPublisher,
                                   @Autowired(required = false) final LogRedactor logRedactor) {
        this.logEventsPublisher = logEventsPublisher;
        this.logRedactor = logRedactor;
    }

    @PostConstruct
    public void register() {
        PUBLISHER.set(this.logEventsPublisher);
        REDACTOR.set(this.logRedactor);
    }

    @PreDestroy
    public void unregister() {
        PUBLISHER.set(null);
        REDACTOR.set(null);
    }

    public static LogEventsPublisher publisher() {
        return PUBLISHER.get();
    }

    public static LogRedactor redactor() {
        return REDACTOR.get();
    }
}
