package org.cryptotrader.logging.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.cryptotrader.testing.library.infrastructure.annotation.IntegrationSuite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CryptoTraderLoggingAutoConfig")
@SpringBootTest(classes = CryptoTraderLoggingAutoConfig.class)
@TestPropertySource(properties = {
    "crypto-trader-logging.level.console=WARN",
    "crypto-trader-logging.level.file=DEBUG"
})
@IntegrationSuite
public class LoggingConfigTest {

    @Autowired
    private CryptoTraderLoggingAutoConfig config;

    @Nested
    @DisplayName("logging configuration")
    class LoggingProperties {
        @Test
        @DisplayName("should configure the console and file appenders")
        void configureTheConsoleAndFileAppenders() {
            final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

            final Appender<ILoggingEvent> consoleAppender = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("CONSOLE");
            assertNotNull(consoleAppender);

            final Appender<ILoggingEvent> allLogsAppender = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("ALL_LOGS");
            assertNotNull(allLogsAppender);

            final Appender<ILoggingEvent> bugsLogsAppender = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("BUGS_LOGS");
            assertNotNull(bugsLogsAppender);
        }
    }

    @Nested
    @DisplayName("application log appender configuration")
    class ApplicationLogKafkaAppenderWiring {
        @Test
        @DisplayName("should attach the Kafka appender to the application logger")
        void attachTheKafkaAppenderToTheApplicationLogger() {
            final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            // TODO: Pull out constants for logger names and appender names.
            final Appender<ILoggingEvent> asyncAppender = context.getLogger("org.cryptotrader")
                    .getAppender("ASYNC_APPLICATION_LOG_KAFKA");
            assertNotNull(asyncAppender);
        }
    }
}
