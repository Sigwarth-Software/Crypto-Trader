package org.cryptotrader.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.cryptotrader.logging.config.LogEventPublisherBridge;
import org.cryptotrader.logging.library.events.ApplicationExceptionEventPayload;
import org.cryptotrader.logging.library.events.ApplicationLogEventPayload;
import org.cryptotrader.logging.library.events.publisher.LogEventsPublisher;
import org.cryptotrader.logging.properties.LogPersistenceMode;
import org.cryptotrader.logging.redaction.LogRedactor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

public class ApplicationLogKafkaAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final String[] RECURSIVE_LOGGER_PREFIXES = {
            "org.apache.kafka",
            "org.springframework.kafka",
            "org.springframework.cloud.stream",
            "org.cryptotrader.universal.library.events.EventPublisher",
            "org.cryptotrader.logging.library.events.publisher.LogEventsPublisher"
    };

    @Override
    protected void append(ILoggingEvent event) {
        if (this.isRecursiveLoggerEvent(event)) {
            return;
        }

        if (!this.isPersistenceEnabled()) {
            return;
        }

        LogEventsPublisher publisher = LogEventPublisherBridge.publisher();
        if (publisher == null) {
            return;
        }

        LogRedactor redactor = LogEventPublisherBridge.redactor();
        String module = this.getContextProperty("ct_app_name", "unknown-service");
        LocalDateTime timestamp = this.toLocalDateTime(event.getTimeStamp());

        String message = this.redactText(redactor, event.getFormattedMessage());
        IThrowableProxy throwableProxy = event.getThrowableProxy();

        publisher.publishApplicationLog(new ApplicationLogEventPayload(
                timestamp,
                event.getLevel().toString(),
                event.getLoggerName(),
                module,
                event.getThreadName(),
                message,
                this.getMdcContext(event),
                throwableProxy != null ? throwableProxy.getClassName() : null,
                throwableProxy != null ? this.redactText(redactor, throwableProxy.getMessage()) : null,
                throwableProxy != null ? this.redactText(redactor, ThrowableProxyUtil.asString(throwableProxy)) : null
        ));

        if (throwableProxy != null) {
            IThrowableProxy rootCause = throwableProxy.getCause();
            while (rootCause != null && rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            publisher.publishApplicationException(new ApplicationExceptionEventPayload(
                    timestamp,
                    module,
                    event.getLoggerName(),
                    event.getLevel().toString(),
                    event.getThreadName(),
                    throwableProxy.getClassName(),
                    this.redactText(redactor, throwableProxy.getMessage()),
                    this.redactText(redactor, ThrowableProxyUtil.asString(throwableProxy)),
                    rootCause != null ? rootCause.getClassName() : null,
                    rootCause != null ? this.redactText(redactor, rootCause.getMessage()) : null
            ));
        }
    }

    private boolean isRecursiveLoggerEvent(ILoggingEvent event) {
        String loggerName = event.getLoggerName();
        for (String prefix : RECURSIVE_LOGGER_PREFIXES) {
            if (loggerName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPersistenceEnabled() {
        String mode = this.getContextProperty("ct_persistence_mode", LogPersistenceMode.DATABASE.name());
        return !LogPersistenceMode.DISK.name().equalsIgnoreCase(mode);
    }

    private String getContextProperty(String name, String defaultValue) {
        if (this.getContext() == null) {
            return defaultValue;
        }
        String value = this.getContext().getProperty(name);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    private String redactText(LogRedactor redactor, String text) {
        String stripped = AnsiStripperConverter.stripEscapeCode(text);
        if (redactor != null) {
            return redactor.redactText(stripped);
        }
        return stripped;
    }

    private Map<String, String> getMdcContext(ILoggingEvent event) {
        Map<String, String> mdcPropertyMap;
        try {
            mdcPropertyMap = event.getMDCPropertyMap();
        } catch (RuntimeException exception) {
            return null;
        }
        return (mdcPropertyMap == null || mdcPropertyMap.isEmpty()) ? null : mdcPropertyMap;
    }

    // TODO: Move to Universal-Scripts.
    private LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}
