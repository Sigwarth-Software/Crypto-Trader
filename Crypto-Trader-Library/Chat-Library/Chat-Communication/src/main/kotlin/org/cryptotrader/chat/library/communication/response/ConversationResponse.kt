package org.cryptotrader.chat.library.communication.response

import java.util.UUID

data class ConversationResponse(
    val conversationId: UUID,
    val title: String,
)
