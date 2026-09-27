package org.cryptotrader.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

/**
 * Strips ANSI escape codes from log messages.
 * This ensures that log files remain clean even when console colors are enabled.
 */
public class AnsiStripperConverter extends CompositeConverter<ILoggingEvent> {
    private static final String ANSI_ESCAPE_REGEX = "\u001B\\[[;\\d]*[A-Za-z]";

    public static String stripEscapeCode(final String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll(ANSI_ESCAPE_REGEX, "");
    }

    @Override
    protected String transform(final ILoggingEvent event, final String input) {
        return stripEscapeCode(input);
    }
}
