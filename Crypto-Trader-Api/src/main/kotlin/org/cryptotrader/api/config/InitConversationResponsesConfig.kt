package org.cryptotrader.api.config

import org.cryptotrader.chat.library.communication.request.InitConversationRequest
import org.cryptotrader.chat.library.communication.response.InitConversationResponse
import org.cryptotrader.chat.library.events.InitConversationGateway
import org.cryptotrader.universal.library.events.config.BaseGatewayConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.function.Consumer

@Configuration
open class InitConversationResponsesConfig(
    gateway: InitConversationGateway
) : BaseGatewayConfig<InitConversationRequest, InitConversationResponse>(gateway) {
    override val log: Logger = LoggerFactory.getLogger(InitConversationResponsesConfig::class.java)

    @Bean(name = ["initConversationResponsesConsumer"])
    open fun initConversationResponsesConsumer(): Consumer<Message<InitConversationResponse>> {
        return this.createConsumer()
    }
}
