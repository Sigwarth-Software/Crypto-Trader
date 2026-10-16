package org.cryptotrader.chat.library.services;

import com.fasterxml.jackson.databind.ObjectMapper
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.chat.library.entity.ChatConversation
import org.cryptotrader.chat.library.entity.ChatConversationMessage
import org.cryptotrader.chat.library.model.MessageAuthorSource
import org.cryptotrader.chat.library.services.entity.ChatConversationEntityService
import org.cryptotrader.chat.library.services.entity.ChatConversationMessageEntityService
import org.cryptotrader.chat.library.model.OpenAiChatCompletionChunk
import org.cryptotrader.chat.library.model.OpenAiChatCompletionRequest
import org.cryptotrader.chat.library.model.OpenAiMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
open class ChatStreamingService @Autowired constructor(
    private val chatWebClient: WebClient,
    private val objectMapper: ObjectMapper,
    private val chatConversationMessageEntityService: ChatConversationMessageEntityService,
    private val chatConversationEntityService: ChatConversationEntityService,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChatStreamingService::class.java)
        private const val MAX_HISTORY_MESSAGES: Int = 40
    }

    private val conversationHistories: ConcurrentHashMap<UUID, MutableList<OpenAiMessage>> = ConcurrentHashMap()

    fun streamChat(
        conversationId: UUID,
        user: ProductUser,
        userPrompt: String,
    ): Flux<String> {
        persistMessage(conversationId, MessageAuthorSource.USER, userPrompt, user)

        val messages: MutableList<OpenAiMessage> = mutableListOf<OpenAiMessage>().apply {
            addAll(getHistory(conversationId))
            add(OpenAiMessage(role = "user", content = userPrompt))
        }

        val request = OpenAiChatCompletionRequest(
            model = "gpt-5.4",
            messages = messages,
            stream = true
        )

        val fullAssistantResponse = StringBuilder()

        return this.chatWebClient.post()
            .uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux<String>()
            .doOnNext { rawChunk ->
                extractDeltaContent(rawChunk)?.let { delta ->
                    fullAssistantResponse.append(delta)
                }
            }
            .doOnComplete {
                log.info("Chat stream completed for conversation: {}", conversationId)
                val assistantResponse: String = fullAssistantResponse.toString()
                if (assistantResponse.isBlank()) {
                    log.warn("Chat stream completed without assistant content for conversation: {}", conversationId)
                } else {
                    this.recordTurn(conversationId, userPrompt, assistantResponse)
                    this.persistMessage(conversationId, MessageAuthorSource.BOT, assistantResponse, user)
                }
            }
            .doOnError { ex ->
                log.error("Error in chat stream for conversation: {}", conversationId, ex)
            }
            .doOnCancel {
                log.warn("Client aborted stream for conversation: {}", conversationId)
            }
    }

    private fun extractDeltaContent(rawChunk: String): String? {
        val json: String = rawChunk.trim().removePrefix("data:").trim()
        if (json.isEmpty() || json == "[DONE]") {
            return null
        }

        return try {
            val chunk = this.objectMapper.readValue(json,
                OpenAiChatCompletionChunk::class.java)
            chunk.choices.firstOrNull()?.delta?.content
        } catch (ex: Exception) {
            log.error("Failed to parse chunk: {}", rawChunk, ex)
            null
        }
    }

    private fun getHistory(conversationId: UUID): List<OpenAiMessage> {
        val history: MutableList<OpenAiMessage> = this.conversationHistories[conversationId] ?: return emptyList()
        return synchronized(history) { history.toList() }
    }

    private fun recordTurn(conversationId: UUID, userPrompt: String, assistantResponse: String) {
        val history: MutableList<OpenAiMessage> = this.conversationHistories.computeIfAbsent(conversationId) { mutableListOf() }
        synchronized(history) {
            history.add(OpenAiMessage(role = "user", content = userPrompt))
            history.add(OpenAiMessage(role = "assistant", content = assistantResponse))
            if (history.size > MAX_HISTORY_MESSAGES) {
                history.subList(0, history.size - MAX_HISTORY_MESSAGES).clear()
            }
        }
    }

    private fun persistConversation(conversationId: UUID, user: ProductUser, initialMessage: String): ChatConversation {
        val choppedMessage = initialMessage.take(50)
        val chatConversation = ChatConversation(
            choppedMessage,
            conversationId,
            user
        )

        return this.chatConversationEntityService.save(chatConversation)
    }

    private fun persistMessage(conversationId: UUID, authorSource: MessageAuthorSource, content: String, productUser: ProductUser) {
        log.info("Persisting message for conversation {}: {}", conversationId, content)

        var foundConversation =
            this.chatConversationEntityService.findByConversationId(
                conversationId
            ) ?: this.persistConversation(conversationId, productUser, content)


        this.chatConversationMessageEntityService.save(
            ChatConversationMessage(
                content,
                foundConversation,
                authorSource
            )
        )
    }
}
