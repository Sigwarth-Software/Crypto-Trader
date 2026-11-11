package org.cryptotrader.logging.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.cryptotrader.logging.config.aspect.TimeTrackingAspect;
import org.cryptotrader.logging.properties.TimeTrackingProperties;
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TimeTrackingAspect")
@UnitSuite
class TimeTrackingAspectTest {

    private final TimeTrackingAspect aspect = new TimeTrackingAspect(null, new TimeTrackingProperties());

    @Nested
    @DisplayName("logExecutionTime")
    class LogExecutionTime {
        @Nested
        @DisplayName("when logging is enabled")
        class WhenLoggingIsEnabled {
            private Logger logger;
            private ListAppender<ILoggingEvent> appender;

            @BeforeEach
            void setUp() throws Exception {
                logger = (Logger) LoggerFactory.getLogger(TimeTrackingAspect.class);
                appender = new ListAppender<>();
                appender.start();
                logger.addAppender(appender);
            }

            @DisplayName("should log execution time")
            @Test
            void logExecutionTime() {
                try {
                    final TrackedService proxy = proxy(new TrackedService());

                    assertThat(proxy.logged()).isEqualTo("tracked");
                    assertThat(appender.list)
                            .extracting(ILoggingEvent::getFormattedMessage)
                            .anySatisfy(message -> assertThat(message)
                                    .contains("TrackedService")
                                    .contains("logged()")
                                    .contains("executed in")
                                    .contains("expected 1ms"));
                } finally {
                    logger.detachAppender(appender);
                }
            }
        }

        @Nested
        @DisplayName("when logging is disabled")
        class WhenLoggingIsDisabled {
            private Logger logger;
            private ListAppender<ILoggingEvent> appender;

            @BeforeEach
            void setUp() throws Exception {
                logger = (Logger) LoggerFactory.getLogger(TimeTrackingAspect.class);
                appender = new ListAppender<>();
                appender.start();
                logger.addAppender(appender);
            }

            @DisplayName("should skip logging")
            @Test
            void skipLogging() {
                try {
                    final TrackedService proxy = proxy(new TrackedService());

                    assertThat(proxy.silent()).isEqualTo("silent");
                    assertThat(appender.list).isEmpty();
                } finally {
                    logger.detachAppender(appender);
                }
            }
        }
    }

    private @NotNull TrackedService proxy(final @NotNull TrackedService target) {
        final AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(aspect);
        return (TrackedService) proxyFactory.getProxy();
    }

    static class TrackedService {
        @TimeTracked
        public @NotNull String logged() {
            return "tracked";
        }

        @TimeTracked(isLogged = false)
        public @NotNull String silent() {
            return "silent";
        }
    }
}
