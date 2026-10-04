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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Map;

import static org.cryptotrader.universal.library.scripts.DateTimeScriptKt.toLocalDateTime;

/** Log appender for sending log events to Kafka. */
public class ApplicationLogKafkaAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private static final String[] RECURSIVE_LOGGER_PREFIXES = {
            "org.apache.kafka",
            "org.springframework.kafka",
            "org.springframework.cloud.stream",
            "org.cryptotrader.universal.library.events.EventPublisher",
            "org.cryptotrader.logging.library.events.publisher.LogEventsPublisher"
    };

    @Override
    protected void append(final @NotNull ILoggingEvent event) {
        if (this.isRecursiveLoggerEvent(event)) {
            return;
        }

        if (!this.isPersistenceEnabled()) {
            return;
        }

        final LogEventsPublisher publisher = LogEventPublisherBridge.publisher();
        if (publisher == null) {
            return;
        }

        final LogRedactor redactor = LogEventPublisherBridge.redactor();
        final String module = this.getContextProperty(
            "ct_app_name",
            "unknown-service"
        );
        final LocalDateTime timestamp = toLocalDateTime(event.getTimeStamp());

        final String message = this.redactText(redactor, event.getFormattedMessage());

        if (message == null) {
            return;
        }

        final IThrowableProxy throwableProxy = event.getThrowableProxy();

        final String errorName =
            throwableProxy != null ? throwableProxy.getClassName() : null;
        final String errorMessage =
            throwableProxy != null ? this.redactText(redactor, throwableProxy.getMessage()) : null;
        final String errorStack =
            throwableProxy != null ? this.redactText(
                redactor,
                ThrowableProxyUtil.asString(throwableProxy)
            ) : null;
        publisher.publishApplicationLog(
            new ApplicationLogEventPayload(
                timestamp,
                event.getLevel().toString(),
                event.getLoggerName(),
                module,
                event.getThreadName(),
                message,
                this.getMdcContext(event),
                errorName,
                errorMessage,
                errorStack
            )
        );

        if (throwableProxy != null) {
            IThrowableProxy rootCause = throwableProxy.getCause();
            while (rootCause != null && rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            publisher.publishApplicationException(
                new ApplicationExceptionEventPayload(
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
                )
            );
        }
    }

    private boolean isRecursiveLoggerEvent(final @NotNull ILoggingEvent event) {
        final String loggerName = event.getLoggerName();

        for (final String prefix : RECURSIVE_LOGGER_PREFIXES) {
            if (loggerName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPersistenceEnabled() {
        final String mode = this.getContextProperty(
            "ct_persistence_mode", LogPersistenceMode.DATABASE.name()
        );
        return !LogPersistenceMode.DISK.name().equalsIgnoreCase(mode);
    }

    private String getContextProperty(@NotNull final String name,
                                      @NotNull final String defaultValue) {
        if (this.getContext() == null) {
            return defaultValue;
        }
        final String value = this.getContext().getProperty(name);

        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    private @Nullable String redactText(final @Nullable LogRedactor redactor,
                                        @NotNull final String text) {
        final String stripped = AnsiStripperConverter.stripEscapeCode(text);

        if (redactor != null) {
            return redactor.redactText(stripped);
        }
        return stripped;
    }

    private @Nullable Map<String, String> getMdcContext(final @NotNull ILoggingEvent event) {
        final Map<String, String> mdcPropertyMap;

        try {
            mdcPropertyMap = event.getMDCPropertyMap();
        } catch (@NotNull final RuntimeException exception) {
            return null;
        }

        if (mdcPropertyMap == null || mdcPropertyMap.isEmpty()) {
            return null;
        }
        return mdcPropertyMap;
    }
}
