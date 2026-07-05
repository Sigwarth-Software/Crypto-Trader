package org.cryptotrader.logging.config.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.cryptotrader.universal.library.model.Ansi;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.cryptotrader.logging.library.events.ExecutionSpeedLogEventPayload;
import org.cryptotrader.logging.library.events.publisher.LogEventsPublisher;
import org.cryptotrader.logging.library.entity.ExecutionSpeedWarningLevel;
import org.cryptotrader.logging.properties.TimeTrackingProperties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

@Aspect
public class TimeTrackingAspect {
    private static final Logger log = LoggerFactory.getLogger(TimeTrackingAspect.class);
    private final LogEventsPublisher logEventsPublisher;
    private final TimeTrackingProperties timeTrackingProperties;

    public TimeTrackingAspect(LogEventsPublisher logEventsPublisher,
                              TimeTrackingProperties timeTrackingProperties) {
        this.logEventsPublisher = logEventsPublisher;
        this.timeTrackingProperties = timeTrackingProperties;
    }

    @Around("@annotation(timeTracked)")
    public Object trackExecutionTime(ProceedingJoinPoint joinPoint, TimeTracked timeTracked) throws Throwable {
        if (timeTracked == null || !timeTracked.isLogged()) {
            return joinPoint.proceed();
        }

        final long startNanos = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            final long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            final long configuredExpectedMillis = timeTracked.expectedMillis();
            long expectedMillis = Math.max(1L, configuredExpectedMillis);
            final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            final String declaringTypeName = signature.getDeclaringTypeName();
            final String className = parseClassName(declaringTypeName);
            final String rawMethodName = signature.getName();
            final String duration = getDurationString(elapsedMillis, expectedMillis);
            final String fullMethodQualifiedName = declaringTypeName + "." + rawMethodName;
            if (timeTracked.shouldPersist()) {
                publishExecutionSpeedLog(elapsedMillis, configuredExpectedMillis, fullMethodQualifiedName, rawMethodName, className);
            }
            final String displayMethodName = getMethodName(className, rawMethodName);
            log.info("{} executed in {} {}(expected {}ms){}", displayMethodName, duration, Ansi.GRAY, expectedMillis, Ansi.RESET);
        }
    }

    private static @NotNull String parseClassName(String declaringTypeName) {
        final int lastDotIndex = declaringTypeName.lastIndexOf('.');
        final String className = lastDotIndex >= 0 ? declaringTypeName.substring(lastDotIndex + 1) : declaringTypeName;
        return className;
    }

    private void publishExecutionSpeedLog(long elapsedMillis,
                                          long configuredExpectedMillis,
                                          String fullMethodQualifiedName,
                                          String methodName,
                                          String className) {
        if (this.logEventsPublisher == null) {
            log.warn("Execution speed logging is not enabled.");
            return;
        }

        try {
            log.debug("Publishing execution speed log for {}", fullMethodQualifiedName);
            this.logEventsPublisher.publish(new ExecutionSpeedLogEventPayload(
                elapsedMillis,
                fullMethodQualifiedName,
                methodName,
                className,
                java.time.LocalDateTime.now(),
                configuredExpectedMillis < 0 ? null : configuredExpectedMillis
            ));
        } catch (RuntimeException exception) {
            log.warn("Failed to publish execution speed log for {}", fullMethodQualifiedName, exception);
        }
    }

    private String getDurationString(long elapsedMillis, long expectedMillis) {
        final ExecutionSpeedWarningLevel warningLevel = ExecutionSpeedWarningLevel.from(
            elapsedMillis,
            expectedMillis,
            this.timeTrackingProperties.getExceedingFactor(),
            this.timeTrackingProperties.getExpectedFactor(),
            this.timeTrackingProperties.getWarningFactor(),
            this.timeTrackingProperties.getAlertFactor()
        );
        final String durationColor = switch (warningLevel) {
            case EXCEEDING -> Ansi.BLUE;
            case EXPECTED -> Ansi.GREEN;
            case WARNING -> Ansi.YELLOW;
            case ALERT -> Ansi.RED;
        };
        final String duration = "%s%dms%s".formatted(durationColor, elapsedMillis, Ansi.RESET);
        return duration;
    }

    private static String getMethodName(String className, String methodName) {
        return "%s%s%s%s.%s%s%s()%s".formatted(Ansi.BOLD,
            className,
            Ansi.RESET,
            Ansi.GRAY,
            Ansi.RESET,
            Ansi.CYAN,
            methodName,
            Ansi.RESET);
    }
}
