package org.cryptotrader.logging.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.logging.properties.CryptoTraderWebSocketLoggingProperties;
import org.cryptotrader.logging.redaction.LogRedactor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;
import java.util.Map;

import static org.cryptotrader.logging.library.scripts.LoggingFormatterScriptKt.asText;
import static org.cryptotrader.logging.library.scripts.LoggingFormatterScriptKt.humanSize;
import static org.cryptotrader.logging.library.scripts.LoggingParsingScriptKt.sizeOf;

@Slf4j
@RequiredArgsConstructor
public class StompChannelLoggingInterceptor implements ChannelInterceptor {

    private final @NotNull CryptoTraderWebSocketLoggingProperties props;
    private final @NotNull LogRedactor logRedactor;

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        try {
            logMessage(message);
        } catch (final Exception ex) {
            log.warn("Failed to log STOMP frame", ex);
        }
        return message;
    }

    private void logMessage(final @NotNull Message<?> message) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return;
        final StompCommand cmd = accessor.getCommand();
        final String sessionId = accessor.getSessionId();
        final Principal user = accessor.getUser();
        final String destination = accessor.getDestination();

        final StringBuilder sb = new StringBuilder(256);
        sb.append(color("[", AnsiColor.BRIGHT_BLACK)).append(color("WS", AnsiColor.CYAN, AnsiStyle.BOLD))
          .append(color("] ", AnsiColor.BRIGHT_BLACK));
        sb.append(color(cmd != null ? cmd.name() : "FRAME", AnsiColor.BLUE, AnsiStyle.BOLD));
        if (destination != null) {
            sb.append(' ').append(color(destination, AnsiColor.WHITE));
        }
        if (sessionId != null) {
            sb.append(' ').append(color("sid=" + sessionId, AnsiColor.BRIGHT_BLACK));
        }
        if (user != null) {
            sb.append(' ').append(color("user=" + user.getName(), AnsiColor.BRIGHT_BLACK));
        }
        final Object payload = message.getPayload();
        final int size = sizeOf(payload);
        sb.append(' ').append(color(humanSize(size), AnsiColor.MAGENTA));

        if (props.isIncludeHeaders()) {
            sb.append('\n').append(color("Headers:", AnsiColor.BRIGHT_BLACK)).append('\n');
            for (final Map.Entry<String, Object> objectEntry : message.getHeaders().entrySet()) {
                final String value = this.logRedactor.redactHeader(objectEntry.getKey(), String.valueOf(objectEntry.getValue()));
                final String formattedKey = color(objectEntry.getKey() + ": ", AnsiColor.BRIGHT_BLACK);
                final String formattedValue = color(value, AnsiColor.WHITE);
                sb.append("  ").append(formattedKey).append(formattedValue).append('\n');
            }
        }
        if (props.isIncludePayload() && payload != null) {
            final String text = asText(payload, props.getMaxPayloadLength());
            if (!text.isEmpty()) {
                final String formattedKey = color("Payload:", AnsiColor.BRIGHT_BLACK);
                final String formattedValue = color(this.logRedactor.redactText(text), AnsiColor.WHITE);
                sb.append('\n').append(formattedKey).append(' ').append(formattedValue);
            }
        }

        // Choose level by command
        if (cmd == StompCommand.ERROR) {
            log.error(sb.toString());
        } else if (cmd == StompCommand.DISCONNECT) {
            log.info(sb.toString());
        } else if (cmd == StompCommand.CONNECT || cmd == StompCommand.CONNECTED) {
            log.info(sb.toString());
        } else if (cmd == StompCommand.SUBSCRIBE || cmd == StompCommand.UNSUBSCRIBE) {
            log.debug(sb.toString());
        } else {
            log.trace(sb.toString());
        }
    }

    private String color(final String text, final AnsiColor color) {
        return color(text, color, null);
    }

    private String color(final String text, final AnsiColor color, @Nullable final AnsiStyle style) {
        if (!this.props.isColorEnabled()) return text;
        if (style != null) {
            return AnsiOutput.toString(style, color, text, AnsiStyle.NORMAL);
        }
        return AnsiOutput.toString(color, text, AnsiColor.DEFAULT);
    }
}
