package org.cryptotrader.development.library.services.client.exchange

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange
interface GitHubGraphQlExchange {
    @PostExchange(
        url = "/graphql",
        contentType = MediaType.APPLICATION_JSON_VALUE
    )
    fun execute(
        @RequestBody request: Map<String, Any>
    ): JsonNode
}