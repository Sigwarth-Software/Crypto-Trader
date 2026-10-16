package org.cryptotrader.chat.library.events

import org.cryptotrader.chat.library.communication.request.InitConversationRequest
import org.cryptotrader.chat.library.communication.response.InitConversationResponse
import org.cryptotrader.universal.library.events.EventPublisher
import org.cryptotrader.universal.library.events.RequestGatewayController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class InitConversationGateway @Autowired constructor(
    override val eventPublisher: EventPublisher
): RequestGatewayController<InitConversationRequest, InitConversationResponse>() {

}