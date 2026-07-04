package org.cryptotrader.agent.library.component

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicStatusLine
import org.apache.http.ProtocolVersion
import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.mock

class HttpFetchToolTest : CryptoTraderTest() {
    @Mock
    lateinit var properties: AgentConstraintsProperties

    @Mock
    lateinit var httpClient: CloseableHttpClient

    @Mock
    lateinit var httpGet: HttpGet

    lateinit var httpFetchTool: HttpFetchTool

    @BeforeEach
    fun setUp() {
        this.httpFetchTool = HttpFetchTool(properties, httpClient, httpGet)
    }

    @Test
    fun `init should set User-Agent header`() {
        verify(httpGet).setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    }

    @Nested
    inner class StripHtml {
        @Test
        fun removesTagsAndDecodesEntities() {
            val html = "<div>Hello &amp; welcome! <p>This is a <b>test</b>.</p></div>"
            val expected = "Hello & welcome! This is a test."
            val result: String = httpFetchTool.stripHtml(html)
            assertEquals(expected, result)
        }
    }
}
