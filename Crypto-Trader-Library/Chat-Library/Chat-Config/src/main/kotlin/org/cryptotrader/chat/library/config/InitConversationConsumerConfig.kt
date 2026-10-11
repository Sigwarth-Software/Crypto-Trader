package org.cryptotrader.chat.library.config

import org.cryptotrader.chat.library.communication.request.InitConversationRequest
import org.cryptotrader.chat.library.communication.response.InitConversationResponse
import org.cryptotrader.chat.library.events.ChatEventBinding
import org.cryptotrader.universal.library.events.BaseEventConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Configuration(proxyBeanMethods = false)
class InitConversationConsumerConfig @Autowired constructor(
    streamBridge: StreamBridge
) : BaseEventConsumer<InitConversationRequest, InitConversationResponse>(
    streamBridge,
    ChatEventBinding.INIT_CONVERSATION_RESPONSES.bindingName
) {
    override val log: Logger = LoggerFactory.getLogger(InitConversationConsumerConfig::class.java)

    override fun handleError(exception: Exception): InitConversationResponse {
        // If it fails, we want it to fail.
        throw exception
    }
}