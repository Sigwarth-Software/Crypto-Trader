package org.cryptotrader.chat.library.services.entity;

import org.cryptotrader.chat.library.entity.ChatConversationMessage
import org.cryptotrader.chat.library.repository.ChatConversationMessageRepository;
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
open class ChatConversationMessageEntityService @Autowired constructor(
    repository:ChatConversationMessageRepository
) : BaseEntityService<ChatConversationMessage, Long, ChatConversationMessageRepository>(repository) {
    fun findByConversationId(conversationId: Long): List<ChatConversationMessage> {
        return this.repository.findByConversationId(conversationId)
    }
}
