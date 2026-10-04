package org.cryptotrader.logging.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.cryptotrader.logging.config.aspect.TimeTrackingAspect;
import org.cryptotrader.logging.properties.TimeTrackingProperties;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;

class TimeTrackingAspectTest {

    private final TimeTrackingAspect aspect = new TimeTrackingAspect(null, new TimeTrackingProperties());

    @Test
    void logsExecutionTimeByDefault() {
        final Logger logger = (Logger) LoggerFactory.getLogger(TimeTrackingAspect.class);
        final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

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

    @Test
    void skipsLoggingWhenDisabled() {
        final Logger logger = (Logger) LoggerFactory.getLogger(TimeTrackingAspect.class);
        final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            final TrackedService proxy = proxy(new TrackedService());

            assertThat(proxy.silent()).isEqualTo("silent");
            assertThat(appender.list).isEmpty();
        } finally {
            logger.detachAppender(appender);
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
