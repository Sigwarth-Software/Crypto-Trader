package org.cryptotrader.agent.library.component

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify

@DisplayName("HttpFetchTool")
@UnitSuite
class HttpFetchToolTest : CryptoTraderTest() {
    @Mock
    lateinit var properties: AgentConstraintsProperties

    @Mock
    lateinit var httpClient: CloseableHttpClient

    @Mock
    lateinit var httpGet: HttpGet

    @InjectMocks
    lateinit var httpFetchTool: HttpFetchTool

    @Nested
    @DisplayName("constructor")
    inner class Constructor {
        @DisplayName("should set the User-Agent header")
        @Test
        fun setTheUserAgentHeader() {
            verify(httpGet).setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        }
    }

    @Nested
    @DisplayName("stripHtml")
    inner class StripHtml {
        @DisplayName("should remove tags and decode entities")
        @Test
        fun removeTagsAndDecodeEntities() {
            val html = "<div>Hello &amp; welcome! <p>This is a <b>test</b>.</p></div>"
            val expected = "Hello & welcome! This is a test."
            val result: String = httpFetchTool.stripHtml(html)
            assertEquals(expected, result)
        }
    }
}
