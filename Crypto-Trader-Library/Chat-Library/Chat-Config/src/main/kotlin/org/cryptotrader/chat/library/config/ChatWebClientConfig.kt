package org.cryptotrader.chat.library.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class ChatWebClientConfig @Autowired constructor(
    @Value($$"${services.chat.base-url:http://localhost:8090}")
    private val chatServiceBaseUrl: String
) {
    @Bean
    fun chatWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(this.chatServiceBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
            .build()
    }
}