package org.cryptotrader.chat.library.config

import org.cryptotrader.chat.library.communication.request.InitConversationRequest
import org.cryptotrader.chat.library.communication.response.InitConversationResponse
import org.cryptotrader.chat.library.events.ChatEventBinding
import org.cryptotrader.universal.library.events.BaseEventConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.UUID
import java.util.function.Consumer

@Configuration(proxyBeanMethods = false)
open class InitConversationConsumerConfig @Autowired constructor(
    streamBridge: StreamBridge
) : BaseEventConsumer<InitConversationRequest, InitConversationResponse>(
    streamBridge,
    ChatEventBinding.INIT_CONVERSATION_RESPONSES.bindingName
) {
    override val log: Logger = LoggerFactory.getLogger(InitConversationConsumerConfig::class.java)

    @Bean(name = ["initConversationConsumer"])
    open fun initConversationConsumer(): Consumer<Message<InitConversationRequest>> {
        return this.createConsumer { request, _ ->
            log.info("Processing conversation init request: {}", request)
            InitConversationResponse(UUID.randomUUID())
        }
    }

    override fun handleError(exception: Exception): InitConversationResponse {
        // If it fails, we want it to fail.
        throw exception
    }
}