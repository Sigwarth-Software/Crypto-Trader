package org.cryptotrader.api.controller

import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.chat.library.communication.request.ConversationMessageRequest
import org.cryptotrader.chat.library.communication.request.InitConversationRequest
import org.cryptotrader.chat.library.communication.response.ConversationMessageResponse
import org.cryptotrader.chat.library.communication.response.ConversationResponse
import org.cryptotrader.chat.library.communication.response.InitConversationResponse
import org.cryptotrader.chat.library.entity.ChatConversation
import org.cryptotrader.chat.library.entity.ChatConversationMessage
import org.cryptotrader.chat.library.events.ChatEventBinding
import org.cryptotrader.chat.library.events.InitConversationGateway
import org.cryptotrader.chat.library.services.ChatStreamingService
import org.cryptotrader.chat.library.services.entity.ChatConversationEntityService
import org.cryptotrader.chat.library.services.entity.ChatConversationMessageEntityService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.UUID
import kotlin.collections.map

@RestController
@RequestMapping("/api/chat")
class ChatController @Autowired constructor(
    private val chatStreamingService: ChatStreamingService,
    private val initConversationGateway: InitConversationGateway,
    private val chatConversationEntityService: ChatConversationEntityService,
    private val chatConversationMessageEntityService: ChatConversationMessageEntityService
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChatController::class.java)
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    fun listConversations(
        @AuthenticationPrincipal user: ProductUser
    ): ResponseEntity<List<ConversationResponse>> {
        if (user.id == null) {
            return ResponseEntity.internalServerError().build()
        }
        val conversations: List<ChatConversation> = this.chatConversationEntityService.findByUserId(user.id!!)
        val responses: List<ConversationResponse> = conversations.map { it: ChatConversation ->
            ConversationResponse(
                it.conversationId,
                it.conversationTitle
            )
        }
        return ResponseEntity.ok(responses)
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/messages")
    fun listMessages(
        @AuthenticationPrincipal user: ProductUser,
        @PathVariable id: String
    ): ResponseEntity<List<ConversationMessageResponse>> {
        if (user.id == null) {
            return ResponseEntity.internalServerError().build()
        }
        val domainConversationId: Long =
            this.chatConversationEntityService.findByConversationId(
                UUID.fromString(id)
            )?.id ?: return ResponseEntity.notFound().build()

        val messages: List<ChatConversationMessage> = this.chatConversationMessageEntityService.findByConversationId(domainConversationId)
        val responses: List<ConversationMessageResponse> = messages.map { it: ChatConversationMessage ->
            ConversationMessageResponse(
                it.messageContent,
                it.authorSource
            )
        }

        log.info("Found {} messages for conversation {} for user {}", responses.size, id, user.id)
        return ResponseEntity.ok(responses)
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/init")
    fun initChat(
        @AuthenticationPrincipal user: ProductUser
    ): ResponseEntity<InitConversationResponse> {
        try {
            val response: InitConversationResponse = this.initConversationGateway.execute(
                ChatEventBinding.INIT_CONVERSATION_REQUESTS,
                InitConversationRequest(user)
            )
            log.info("Initialized chat {} for user {}", response.conversationId, user.id)
            return ResponseEntity.ok(response)
        } catch (exception: Exception) {
            log.error("Error initializing chat: {}", exception.toString())
            return ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/{id}/send")
    fun sendMessage(@PathVariable("id") id: UUID) {

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(
        "/{id}/stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun streamMessage(
        @PathVariable("id") id: UUID,
        @RequestBody request: ConversationMessageRequest,
        @AuthenticationPrincipal user: ProductUser
    ): ResponseEntity<Flux<String>> {
        val streamFlux: Flux<String> = this.chatStreamingService.streamChat(
            conversationId = id,
            user = user,
            userPrompt = request.messageContent
        )

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
            .header(HttpHeaders.CACHE_CONTROL, "no-cache")
            .header(HttpHeaders.CONNECTION, "keep-alive")
            .header("X-Accel-Buffering", "no")
            .body(streamFlux)
    }
}