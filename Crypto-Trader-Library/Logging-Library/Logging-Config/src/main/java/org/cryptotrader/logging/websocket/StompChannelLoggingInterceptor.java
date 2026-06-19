package org.cryptotrader.logging.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.logging.properties.CryptoTraderWebSocketLoggingProperties;
import org.cryptotrader.logging.redaction.LogRedactor;
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

import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class StompChannelLoggingInterceptor implements ChannelInterceptor {

    private final CryptoTraderWebSocketLoggingProperties props;
    private final LogRedactor logRedactor;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            logMessage(message);
        } catch (Exception ex) {
            log.warn("Failed to log STOMP frame", ex);
        }
        return message;
    }

    private void logMessage(Message<?> message) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return;
        StompCommand cmd = accessor.getCommand();
        String sessionId = accessor.getSessionId();
        Principal user = accessor.getUser();
        String destination = accessor.getDestination();

        StringBuilder sb = new StringBuilder(256);
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
        Object payload = message.getPayload();
        int size = sizeOf(payload);
        sb.append(' ').append(color(humanSize(size), AnsiColor.MAGENTA));

        if (props.isIncludeHeaders()) {
            sb.append('\n').append(color("Headers:", AnsiColor.BRIGHT_BLACK)).append('\n');
            for (Map.Entry<String, Object> objectEntry : message.getHeaders().entrySet()) {
                String value = this.logRedactor.redactHeader(objectEntry.getKey(), String.valueOf(objectEntry.getValue()));
                sb.append("  ").append(color(objectEntry.getKey() + ": ", AnsiColor.BRIGHT_BLACK)).append(color(value, AnsiColor.WHITE)).append('\n');
            }
        }
        if (props.isIncludePayload() && payload != null) {
            String text = asText(payload, props.getMaxPayloadLength());
            if (!text.isEmpty()) {
                sb.append('\n').append(color("Payload:", AnsiColor.BRIGHT_BLACK)).append(' ').append(color(this.logRedactor.redactText(text), AnsiColor.WHITE));
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

    private int sizeOf(@Nullable Object payload) {
        if (payload == null) return 0;
        if (payload instanceof byte[] bytes) return bytes.length;
        String s = payload.toString();
        return s.getBytes(Charset.defaultCharset()).length;
    }

    // TODO: Move to script.
    private String humanSize(int bytes) {
        if (bytes < 1024) return bytes + "B";
        int kb = bytes / 1024;
        if (kb < 1024) return kb + "KB";
        int mb = kb / 1024;
        return mb + "MB";
    }

    private String asText(Object payload, int max) {
        if (payload instanceof byte[] bytes) {
            int len = Math.min(bytes.length, max);
            String s = new String(bytes, 0, len, Charset.defaultCharset());
            if (bytes.length > max) s += "…";
            return s;
        }
        String s = Objects.toString(payload, "");
        if (s.length() > max) return s.substring(0, max) + "…";
        return s;
    }

    private String color(String text, AnsiColor color) {
        return color(text, color, null);
    }

    private String color(String text, AnsiColor color, @Nullable AnsiStyle style) {
        if (!this.props.isColorEnabled()) return text;
        if (style != null) {
            return AnsiOutput.toString(style, color, text, AnsiStyle.NORMAL);
        }
        return AnsiOutput.toString(color, text, AnsiColor.DEFAULT);
    }
}
