package org.cryptotrader.chat.library.events

import org.cryptotrader.universal.library.events.model.EventBinding

enum class ChatEventBinding(override val bindingName: String) : EventBinding {
    INIT_CONVERSATION_REQUESTS("initConversationRequests-out-0"),
    INIT_CONVERSATION_RESPONSES("initConversationResponses-out-0"),

    CHAT_MESSAGE_REQUESTS("messageRequests-out-0"),
    CHAT_MESSAGE_RESPONSES("messageResponses-out-0")
}