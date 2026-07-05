package org.cryptotrader.testing.library.infrastructure.logging;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LoggingTestWatcher implements TestWatcher {
    private static final Logger log = LoggerFactory.getLogger(LoggingTestWatcher.class);
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String UNDERLINE = "\u001B[4m";
    private static final String BOLD = "\u001B[1m";

    @Override
    public void testSuccessful(ExtensionContext context) {
        logTestContext(context);
        log.info("{}[PASS]{}: {}.{}()", GREEN, RESET, getClassName(context), getMethodName(context));
        logSeparator();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        logTestContext(context);
        log.error("{}[FAIL]{}: {}.{}() - {}", RED, RESET, getClassName(context), getMethodName(context), cause);
        logSeparator();
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        logTestContext(context);
        log.warn("{}[DISABLED]{}: {}.{}() - {}", YELLOW, RESET, getClassName(context), getMethodName(context), reason.orElse("no reason"));
        logSeparator();
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        logTestContext(context);
        log.warn("{}[ABORTED]{}: {}.{}() - {}", BLUE, RESET, getClassName(context), getMethodName(context), cause);
        logSeparator();
    }

    public void logSeparator() {
        log.info("-".repeat(55));
    }

    private static void logTestContext(ExtensionContext context) {
        log.info("{}{} - {}{}{}", BOLD, getClassName(context), UNDERLINE, context.getDisplayName(), RESET);
    }

    private static @NotNull String getMethodName(ExtensionContext context) {
        return context.getRequiredTestMethod().getName();
    }

    private static @NotNull String getClassName(ExtensionContext context) {
        return context.getRequiredTestClass().getSimpleName();
    }
}
