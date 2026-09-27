package org.cryptotrader.logging.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CryptoTraderLoggingAutoConfig.class)
@TestPropertySource(properties = {
    "crypto-trader-logging.level.console=WARN",
    "crypto-trader-logging.level.file=DEBUG"
})
public class LoggingConfigTest {

    @Autowired
    private CryptoTraderLoggingAutoConfig config;

    @Nested
    @DisplayName("Logging Properties")
    class LoggingProperties {
        @Test
        @DisplayName("testLoggingPropertiesAreApplied")
        void testLoggingPropertiesAreApplied() {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

            Appender<ILoggingEvent> consoleAppender = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("CONSOLE");
            assertNotNull(consoleAppender);

            Appender<ILoggingEvent> allLogsAppender = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("ALL_LOGS");
            assertNotNull(allLogsAppender);

            Appender<ILoggingEvent> bugsLogsAppender = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("BUGS_LOGS");
            assertNotNull(bugsLogsAppender);
        }
    }

    @Nested
    @DisplayName("Application Log Kafka Appender")
    class ApplicationLogKafkaAppenderWiring {
        @Test
        @DisplayName("testApplicationLogKafkaAppenderIsAttachedToCryptotraderLogger")
        void testApplicationLogKafkaAppenderIsAttachedToCryptotraderLogger() {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            // TODO: Pull out constants for logger names and appender names.
            Appender<ILoggingEvent> asyncAppender = context.getLogger("org.cryptotrader")
                    .getAppender("ASYNC_APPLICATION_LOG_KAFKA");
            assertNotNull(asyncAppender);
        }
    }
}
