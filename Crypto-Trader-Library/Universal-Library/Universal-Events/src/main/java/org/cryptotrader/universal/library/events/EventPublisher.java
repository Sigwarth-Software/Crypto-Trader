package org.cryptotrader.universal.library.events;

import lombok.extern.slf4j.Slf4j;
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
    public EventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public <T> boolean publish(String bindingName, T payload) {
        Message<T> message = MessageBuilder
                .withPayload(payload)
                .setHeader(MessageHeaders.CONTENT_TYPE, ENCRYPTED_JSON_CONTENT_TYPE)
                .setHeader(EVENT_BINDING_HEADER, bindingName)
                .build();

        this.logPublish(bindingName, payload);
        return this.streamBridge.send(bindingName, message);
    }

    public <T> boolean publish(String bindingName, T payload, Map<String, Object> headers) {
        Message<T> message = MessageBuilder
                .withPayload(payload)
                .copyHeaders(headers)
                .setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, ENCRYPTED_JSON_CONTENT_TYPE)
                .setHeader(EVENT_BINDING_HEADER, bindingName)
                .build();

        this.logPublish(bindingName, payload);
        return this.streamBridge.send(bindingName, message);
    }

    private void logPublish(String bindingName, Object payload) {
        log.debug("Publishing event to binding '{}' with payload: \n{}", bindingName, payload);
    }
}
