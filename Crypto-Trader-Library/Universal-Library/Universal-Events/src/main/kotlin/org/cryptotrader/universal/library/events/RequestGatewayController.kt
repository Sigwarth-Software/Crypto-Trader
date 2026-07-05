package org.cryptotrader.universal.library.events

import org.cryptotrader.universal.library.events.alias.GatewayResponses
import org.cryptotrader.universal.library.events.model.EventBinding
import org.cryptotrader.universal.library.events.model.RequestGateway
import org.springframework.messaging.Message
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

abstract class RequestGatewayController<Request, Response> :
    RequestGateway<Request, Response> {
    protected open val pendingResponses: GatewayResponses<Response> = ConcurrentHashMap()
    abstract val eventPublisher: EventPublisher

    protected open val defaultTimeout: Duration = Duration.ofSeconds(15)

    fun getCorrelationId(): String {
        return UUID.randomUUID().toString()
    }

    fun getInitialHeaders(correlationId: String, authorizationHeader: String?): Map<String, Any> {
        val headers = mutableMapOf<String, Any>()
        if (!authorizationHeader.isNullOrBlank()) {
            headers["Authorization"] = authorizationHeader
        }
        headers["correlationId"] = correlationId
        return headers
    }

    fun getFutureResponse(): CompletableFuture<Response> {
        return CompletableFuture<Response>()
    }

    fun setPendingResponse(correlationId: String, future: CompletableFuture<Response>) {
        this.pendingResponses[correlationId] = future
    }

    fun tryExecuteAndWait(
        future: CompletableFuture<Response>,
        correlationId: String,
        timeout: Duration
    ): Response {
        return try {
            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
        } catch (exception: TimeoutException) {
            this.pendingResponses.remove(correlationId)
            throw exception
        } catch (exception: Exception) {
            this.pendingResponses.remove(correlationId)
            throw exception
        }
    }

    fun handleResponse(message: Message<Response>) {
        val correlationHeader = message.headers["correlationId"]
        val correlationId: String? = when (correlationHeader) {
            is String -> correlationHeader
            is ByteArray -> try {
                String(correlationHeader, Charsets.UTF_8)
            } catch (e: Exception) {
                null
            }
            else -> null
        }
        if (correlationId == null) {
            return
        }
        val future: CompletableFuture<Response>? = this.pendingResponses.remove(correlationId)
        future?.complete(message.payload)
    }

    override fun execute(binding: EventBinding, request: Request): Response {
        return this.execute(binding, request, defaultTimeout)
    }

    override fun execute(binding: EventBinding, request: Request, timeout: Duration): Response {
        return this.execute(binding, request, timeout, null)
    }

    override fun execute(
        binding: EventBinding,
        request: Request,
        timeout: Duration,
        authorizationHeader: String?
    ): Response {

        val correlationId: String = this.getCorrelationId()
        val future = this.getFutureResponse()
        this.setPendingResponse(correlationId, future)
        val headers = this.getInitialHeaders(correlationId, authorizationHeader)
        this.eventPublisher.publish(binding.bindingName, request, headers)

        return try {
            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
        } catch (exception: TimeoutException) {
            this.pendingResponses.remove(correlationId)
            throw exception
        } catch (exception: Exception) {
            this.pendingResponses.remove(correlationId)
            throw exception
        }
    }
}