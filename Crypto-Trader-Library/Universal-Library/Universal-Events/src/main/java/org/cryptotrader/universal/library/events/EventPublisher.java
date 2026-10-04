package org.cryptotrader.universal.library.events;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@ConditionalOnProperty(prefix = "spring.cloud.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
@Component
public class EventPublisher {
    private static final String EVENT_BINDING_HEADER = "ct-event-binding";
    private static final String ENCRYPTED_JSON_CONTENT_TYPE = "application/vnd.cryptotrader.encrypted+json";
    private final StreamBridge streamBridge;

    @Autowired
    public EventPublisher(final StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public <T> boolean publish(final String bindingName, final @NotNull T payload) {
        final Message<T> message = MessageBuilder
                .withPayload(payload)
                .setHeader(MessageHeaders.CONTENT_TYPE, ENCRYPTED_JSON_CONTENT_TYPE)
                .setHeader(EVENT_BINDING_HEADER, bindingName)
                .build();

        this.logPublish(bindingName, payload);
        return this.streamBridge.send(bindingName, message);
    }

    public <T> boolean publish(final String bindingName, final @NotNull T payload, final Map<String, Object> headers) {
        final Message<T> message = MessageBuilder
                .withPayload(payload)
                .copyHeaders(headers)
                .setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, ENCRYPTED_JSON_CONTENT_TYPE)
                .setHeader(EVENT_BINDING_HEADER, bindingName)
                .build();

        this.logPublish(bindingName, payload);
        return this.streamBridge.send(bindingName, message);
    }

    private void logPublish(final String bindingName, final Object payload) {
        log.debug("Publishing event to binding '{}' with payload: \n{}", bindingName, payload);
    }
}
