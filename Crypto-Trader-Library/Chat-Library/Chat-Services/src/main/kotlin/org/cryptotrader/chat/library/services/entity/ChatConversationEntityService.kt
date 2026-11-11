package org.cryptotrader.chat.library.services.entity

import org.cryptotrader.chat.library.entity.ChatConversation
import org.cryptotrader.chat.library.repository.ChatConversationRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
open class ChatConversationEntityService @Autowired constructor(
    repository: ChatConversationRepository
) : BaseEntityService<ChatConversation, Long, ChatConversationRepository>(repository) {
    fun findByConversationId(conversationId: UUID): ChatConversation? {
        return repository.findByConversationId(conversationId)
    }

    fun findByUserId(productUserId: Long): List<ChatConversation> {
        return repository.findByUserId(productUserId)
    }
}