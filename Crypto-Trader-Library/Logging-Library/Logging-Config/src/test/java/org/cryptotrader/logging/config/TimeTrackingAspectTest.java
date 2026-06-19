package org.cryptotrader.logging.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.cryptotrader.logging.config.aspect.TimeTrackingAspect;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;

class TimeTrackingAspectTest {

    private final TimeTrackingAspect aspect = new TimeTrackingAspect(null);

    @Test
    void logsExecutionTimeByDefault() {
        Logger logger = (Logger) LoggerFactory.getLogger(TimeTrackingAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            TrackedService proxy = proxy(new TrackedService());

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
        Logger logger = (Logger) LoggerFactory.getLogger(TimeTrackingAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            TrackedService proxy = proxy(new TrackedService());

            assertThat(proxy.silent()).isEqualTo("silent");
            assertThat(appender.list).isEmpty();
        } finally {
            logger.detachAppender(appender);
        }
    }

    private TrackedService proxy(TrackedService target) {
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(aspect);
        return (TrackedService) proxyFactory.getProxy();
    }

    static class TrackedService {
        @TimeTracked
        public String logged() {
            return "tracked";
        }

        @TimeTracked(isLogged = false)
        public String silent() {
            return "silent";
        }
    }
}
