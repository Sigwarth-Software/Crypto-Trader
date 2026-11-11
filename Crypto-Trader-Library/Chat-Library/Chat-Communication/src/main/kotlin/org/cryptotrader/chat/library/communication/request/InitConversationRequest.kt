package org.cryptotrader.chat.library.communication.request

import org.cryptotrader.api.library.entity.user.ProductUser

data class InitConversationRequest(
    val conversationUser: ProductUser,
)
