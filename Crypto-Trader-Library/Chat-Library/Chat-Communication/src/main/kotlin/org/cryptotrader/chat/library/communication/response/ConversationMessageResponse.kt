package org.cryptotrader.chat.library.communication.response

import org.cryptotrader.chat.library.model.MessageAuthorSource

data class ConversationMessageResponse(
    val messageContent: String,
    val author: MessageAuthorSource
)
