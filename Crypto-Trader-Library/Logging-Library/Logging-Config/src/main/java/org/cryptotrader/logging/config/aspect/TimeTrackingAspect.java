package org.cryptotrader.logging.config.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.cryptotrader.universal.library.model.Ansi;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.cryptotrader.logging.library.events.ExecutionSpeedLogEventPayload;
import org.cryptotrader.logging.library.events.publisher.LogEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

@Aspect
public class TimeTrackingAspect {
    private static final Logger log = LoggerFactory.getLogger(TimeTrackingAspect.class);
    private final LogEventsPublisher logEventsPublisher;

    public TimeTrackingAspect(LogEventsPublisher logEventsPublisher) {
        this.logEventsPublisher = logEventsPublisher;
    }

    @Around("@annotation(timeTracked)")
    public Object trackExecutionTime(ProceedingJoinPoint joinPoint, TimeTracked timeTracked) throws Throwable {
        if (timeTracked == null || !timeTracked.isLogged()) {
            return joinPoint.proceed();
        }

        long startNanos = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            long configuredExpectedMillis = timeTracked.expectedMillis();
            long expectedMillis = Math.max(1L, configuredExpectedMillis);
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String declaringTypeName = signature.getDeclaringTypeName();
            int lastDotIndex = declaringTypeName.lastIndexOf('.');
            String className = lastDotIndex >= 0 ? declaringTypeName.substring(lastDotIndex + 1) : declaringTypeName;
            String rawMethodName = signature.getName();
            String duration = getDurationString(elapsedMillis, expectedMillis);
            String fullMethodQualifiedName = declaringTypeName + "." + rawMethodName;
            if (timeTracked.shouldPersist()) {
                publishExecutionSpeedLog(elapsedMillis, configuredExpectedMillis, fullMethodQualifiedName, rawMethodName, className);
            }
            String displayMethodName = getMethodName(className, rawMethodName);
            log.info("{} executed in {} {}(expected {}ms){}", displayMethodName, duration, Ansi.GRAY, expectedMillis, Ansi.RESET);
        }
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
            log.info("Publishing execution speed log for {}", fullMethodQualifiedName);
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

    private static String getDurationString(long elapsedMillis, long expectedMillis) {
        String durationColor = elapsedMillis <= expectedMillis * 2 ? Ansi.GREEN
            : elapsedMillis <= expectedMillis * 3 ? Ansi.YELLOW
            : elapsedMillis <= expectedMillis * 6 ? Ansi.RED
            : Ansi.WHITE;
        String duration = durationColor + elapsedMillis + "ms" + Ansi.RESET;
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
