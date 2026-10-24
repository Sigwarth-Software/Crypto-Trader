package org.cryptotrader.logging.config;

import java.time.LocalDateTime;
import org.cryptotrader.logging.library.entity.ExecutionSpeedWarningLevel;
import org.cryptotrader.logging.library.events.ExecutionSpeedLogEventPayload;
import org.cryptotrader.logging.library.service.ExecutionSpeedLogService;
import org.cryptotrader.logging.properties.TimeTrackingProperties;
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.support.GenericMessage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("ExecutionSpeedLogConsumerConfig")
@UnitSuite
class ExecutionSpeedLogConsumerConfigTest {

    @Nested
    @DisplayName("executionSpeedLogsConsumer")
    class ExecutionSpeedLogsConsumer {
        @DisplayName("should classify the over-resourced method before persisting")
        @Test
        void classifyBeforePersisting() {
            final ExecutionSpeedLogService persistenceService = mock(ExecutionSpeedLogService.class);
            final TimeTrackingProperties properties = new TimeTrackingProperties();
            final ExecutionSpeedLogConsumerConfig config = new ExecutionSpeedLogConsumerConfig(persistenceService, properties);
            final ExecutionSpeedLogEventPayload payload = payload(80L, 100L);

            config.executionSpeedLogsConsumer().accept(new GenericMessage<>(payload));

            verify(persistenceService).persist(payload, ExecutionSpeedWarningLevel.EXCEEDING);
        }
    }

    private @NotNull ExecutionSpeedLogEventPayload payload(final long executionSpeed, final long expectedExecutionSpeed) {
        return new ExecutionSpeedLogEventPayload(
            executionSpeed,
            "org.cryptotrader.Example.execute",
            "execute",
            "Example",
            LocalDateTime.now(),
            expectedExecutionSpeed
        );
    }
}
