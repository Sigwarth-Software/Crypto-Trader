package org.cryptotrader.chat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.annotation.PostConstruct
import org.cryptotrader.chat.library.model.OpenAiAssistantMessage
import org.cryptotrader.chat.library.model.OpenAiChatCompletionChunk
import org.cryptotrader.chat.library.model.OpenAiChatCompletionRequest
import org.cryptotrader.chat.library.model.OpenAiChatCompletionResponse
import org.cryptotrader.chat.library.model.OpenAiChoice
import org.cryptotrader.chat.library.model.OpenAiChunkChoice
import org.cryptotrader.chat.library.model.OpenAiDelta
import org.cryptotrader.chat.library.model.OpenAiMessage
import org.cryptotrader.chat.library.model.OpenAiModel
import org.cryptotrader.chat.library.model.OpenAiModelListResponse
import org.cryptotrader.chat.library.model.OpenAiUsage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.*
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.ai.tool.ToolCallback
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/v1")
class OpenAiProxyController(
    private val chatClientBuilder: ChatClient.Builder,
    private val mcpToolCallbackProvider: SyncMcpToolCallbackProvider,
    @Value("\${chat.model.default:gpt-5.4}") private val defaultModel: String
) {
    private val log: Logger = LoggerFactory.getLogger(OpenAiProxyController::class.java)
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())

    // TODO: Clean up code.

    @PostConstruct
    fun init() {
        val tools: Array<out ToolCallback> = this.mcpToolCallbackProvider.getToolCallbacks()
        log.debug("MCP tool callbacks discovered: ${tools.size}")
        tools.forEach { tool ->
            log.debug("Tool: ${tool.toolDefinition.name()}")
        }
    }

    @PostMapping("/chat/completions")
    fun chatCompletions(@RequestBody request: OpenAiChatCompletionRequest): ResponseEntity<Any> {
        val requestedModel: String = request.model ?: this.defaultModel
        val createdTimestampSeconds: Long = Instant.now().epochSecond
        val responseId = "chatcmpl-${UUID.randomUUID()}"

        val springMessages: List<Message> = request.messages.mapNotNull(::toSpringAiMessage)
        val chatClient: ChatClient = this.chatClientBuilder.build()

        return if (request.stream == true) {
            val stream: Flux<String> = streamChatCompletions(
                chatClient = chatClient,
                springMessages = springMessages,
                responseId = responseId,
                createdTimestampSeconds = createdTimestampSeconds,
                model = requestedModel
            )

            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.CONNECTION, "keep-alive")
                .header("X-Accel-Buffering", "no")
                .body(stream)
        } else {
            val chatResponse: ChatResponse? = chatClient.prompt()
                .messages(springMessages)
                .toolCallbacks(this.mcpToolCallbackProvider)
                .call()
                .chatResponse()

            val assistantText: String = chatResponse
                ?.results
                ?.firstOrNull()
                ?.output
                ?.text
                ?: ""

            val finishReason: String = chatResponse
                ?.results
                ?.firstOrNull()
                ?.metadata
                ?.finishReason
                ?: "stop"

            val usage: OpenAiUsage? = chatResponse?.metadata?.usage?.let { usage ->
                OpenAiUsage(
                    promptTokens = usage.promptTokens?.toInt() ?: 0,
                    completionTokens = usage.completionTokens?.toInt() ?: 0,
                    totalTokens = usage.totalTokens?.toInt() ?: 0
                )
            }

            val response = OpenAiChatCompletionResponse(
                id = responseId,
                created = createdTimestampSeconds,
                model = requestedModel,
                choices = listOf(
                    OpenAiChoice(
                        index = 0,
                        message = OpenAiAssistantMessage(content = assistantText),
                        finishReason = finishReason
                    )
                ),
                usage = usage
            )

            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response)
        }
    }

    private fun streamChatCompletions(
        chatClient: ChatClient,
        springMessages: List<Message>,
        responseId: String,
        createdTimestampSeconds: Long,
        model: String
    ): Flux<String> {
        // Shared state for the stream
        var actualId: String = responseId
        val actualCreated: Long = createdTimestampSeconds
        val lastModel: String = model

        val initialChunk = OpenAiChatCompletionChunk(
            id = actualId,
            created = actualCreated,
            model = lastModel,
            choices = listOf(
                OpenAiChunkChoice(
                    index = 0,
                    delta = OpenAiDelta(role = "assistant", content = ""),
                    finishReason = null
                )
            )
        )

        val responseFlux: Flux<ChatResponse> = chatClient.prompt()
            .messages(springMessages)
            .toolCallbacks(mcpToolCallbackProvider)
            .stream()
            .chatResponse()

        val contentChunks: Flux<String> = responseFlux.map { response ->
            // Update shared state from the first real upstream response if possible
            if (response.metadata.id != null) {
                actualId = response.metadata.id
            }
            // Note: Spring AI ChatResponse metadata created might be a Long or null depending on provider

            val chunk = OpenAiChatCompletionChunk(
                id = actualId,
                created = actualCreated,
                model = lastModel,
                choices = response.results.mapIndexed { index, result ->
                    OpenAiChunkChoice(
                        index = index,
                        delta = OpenAiDelta(
                            content = result.output?.text ?: ""
                        ),
                        finishReason = null
                    )
                }
            )
            objectMapper.writeValueAsString(chunk)
        }

        // Dedicated stop chunk
        val stopChunk: Flux<String> = Flux.defer {
            val chunk = OpenAiChatCompletionChunk(
                id = actualId,
                created = actualCreated,
                model = lastModel,
                choices = listOf(
                    OpenAiChunkChoice(
                        index = 0,
                        delta = OpenAiDelta(),
                        finishReason = "stop"
                    )
                )
            )
            Flux.just(this.objectMapper.writeValueAsString(chunk))
        }

        return Flux.concat(
            Flux.just(this.objectMapper.writeValueAsString(initialChunk)),
            contentChunks,
            stopChunk,
            Flux.just("[DONE]")
        )
    }

    @GetMapping("/models")
    fun listModels(): OpenAiModelListResponse {
        return OpenAiModelListResponse(
            objectName = "list",
            data = listOf(
                OpenAiModel(
                    id = "gpt-5.4",
                    created = 1715385600,
                    ownedBy = "openai"
                ),
                OpenAiModel(
                    id = "gpt-5.4-mini",
                    created = 1715385600,
                    ownedBy = "openai"
                ),
                OpenAiModel(
                    id = "gpt-5.4-nano",
                    created = 1715385600,
                    ownedBy = "openai"
                ),
                OpenAiModel(
                    id = "gpt-5.1",
                    created = 1715385600,
                    ownedBy = "openai"
                ),
                OpenAiModel(
                    id = "gpt-5.1-chat-latest",
                    created = 1715385600,
                    ownedBy = "openai"
                ),
                OpenAiModel(
                    id = "gpt-4o",
                    created = 1715385600,
                    ownedBy = "openai"
                ),
                OpenAiModel(
                    id = "gpt-4o-mini",
                    created = 1715385600,
                    ownedBy = "openai"
                )
            )
        )
    }

    private fun sseHeaders(): HttpHeaders = HttpHeaders().apply {
        cacheControl = "no-cache"
        set("Connection", "keep-alive")
    }

    private fun toSpringAiMessage(openAiMessage: OpenAiMessage): Message? {
        val contentText: String = when (val content = openAiMessage.content) {
            null -> ""
            is String -> content
            else -> content.toString()
        }

        return when (openAiMessage.role.lowercase()) {
            "system" -> SystemMessage(contentText)
            "user" -> UserMessage(contentText)
            "assistant" -> AssistantMessage(contentText)
            "tool" -> ToolResponseMessage.builder()
                .responses(
                    listOf(
                        ToolResponseMessage.ToolResponse(
                            openAiMessage.toolCallId ?: "toolcall-${UUID.randomUUID()}",
                            openAiMessage.name ?: "tool",
                            contentText
                        )
                    )
                )
                .build()
            else -> UserMessage(contentText)
        }
    }
}
