package org.cryptotrader.universal.library.events.model

import java.time.Duration

interface RequestGateway<Request, Response> {
    fun execute(binding: EventBinding, request: Request): Response
    fun execute(binding: EventBinding, request: Request, timeout: Duration): Response
    fun execute(binding: EventBinding, request: Request, timeout: Duration, authorizationHeader: String?): Response
}