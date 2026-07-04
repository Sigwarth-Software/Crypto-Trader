package org.cryptotrader.logging.config;

import org.cryptotrader.logging.library.entity.ExecutionSpeedWarningLevel;
import org.cryptotrader.logging.library.events.ExecutionSpeedLogEventPayload;
import org.cryptotrader.logging.library.service.ExecutionSpeedLogService;
import org.cryptotrader.logging.properties.TimeTrackingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.support.GenericMessage;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExecutionSpeedLogConsumerConfigTest {

    @Test
    void classifiesOverResourcedMethodBeforePersisting() {
        ExecutionSpeedLogService persistenceService = mock(ExecutionSpeedLogService.class);
        TimeTrackingProperties properties = new TimeTrackingProperties();
        ExecutionSpeedLogConsumerConfig config = new ExecutionSpeedLogConsumerConfig(persistenceService, properties);
        ExecutionSpeedLogEventPayload payload = payload(80L, 100L);

        config.executionSpeedLogsConsumer().accept(new GenericMessage<>(payload));

        verify(persistenceService).persist(payload, ExecutionSpeedWarningLevel.EXCEEDING);
    }

    private ExecutionSpeedLogEventPayload payload(long executionSpeed, long expectedExecutionSpeed) {
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
