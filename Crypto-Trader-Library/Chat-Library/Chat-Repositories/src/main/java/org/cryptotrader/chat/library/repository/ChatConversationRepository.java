package org.cryptotrader.chat.library.repository;

import org.cryptotrader.chat.library.entity.ChatConversation;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Repository for managing chat conversations. */
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    ChatConversation findByConversationId(UUID conversationId);

    @NotNull List<ChatConversation> findByUserId(long productUserId);
}
