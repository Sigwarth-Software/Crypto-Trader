package org.cryptotrader.chat.library.repository;

import org.cryptotrader.chat.library.entity.ChatConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repository for managing chat conversation messages. */
public interface ChatConversationMessageRepository extends JpaRepository<ChatConversationMessage, Long> {
    List<ChatConversationMessage> findByConversationId(Long conversationId);
}
