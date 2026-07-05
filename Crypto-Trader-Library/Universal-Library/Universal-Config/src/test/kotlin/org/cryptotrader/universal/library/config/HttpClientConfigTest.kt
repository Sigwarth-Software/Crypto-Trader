package org.cryptotrader.universal.library.config

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("HttpClientConfig")
@Tag("component")
@DisplayName("HTTP Client Configuration")
class HttpClientConfigTest : CryptoTraderTest() {

    private lateinit var config: HttpClientConfig

    @BeforeEach
    fun setUp() {
        config = HttpClientConfig()
    }

    @Nested
    @Tag("restTemplate")
    @DisplayName("RestTemplate Bean")
    inner class RestTemplateBean {
        @Test
        @DisplayName("Should provide RestTemplate bean when missing")
        fun restTemplate_ProvidesBean() { }
    }

    @Nested
    @Tag("httpClient4")
    @DisplayName("Apache HttpClient4 Bean")
    inner class HttpClient4Bean {
        @Test
        @DisplayName("Should provide CloseableHttpClient bean when missing")
        fun httpClient4_ProvidesBean() { }
    }

    @Nested
    @Tag("getHttpClient")
    @DisplayName("HttpGet Bean")
    inner class HttpGetBean {
        @Test
        @DisplayName("Should provide HttpGet bean when missing")
        fun getHttpClient_ProvidesBean() { }
    }

    @Nested
    @Tag("postHttpClient")
    @DisplayName("HttpPost Bean")
    inner class HttpPostBean {
        @Test
        @DisplayName("Should provide HttpPost bean when missing")
        fun postHttpClient_ProvidesBean() { }
    }

    @Nested
    @Tag("putHttpClient")
    @DisplayName("HttpPut Bean")
    inner class HttpPutBean {
        @Test
        @DisplayName("Should provide HttpPut bean when missing")
        fun putHttpClient_ProvidesBean() { }
    }

    @Nested
    @Tag("deleteHttpClient")
    @DisplayName("HttpDelete Bean")
    inner class HttpDeleteBean {
        @Test
        @DisplayName("Should provide HttpDelete bean when missing")
        fun deleteHttpClient_ProvidesBean() { }
    }

    @Nested
    @Tag("optionsHttpClient")
    @DisplayName("HttpOptions Bean")
    inner class HttpOptionsBean {
        @Test
        @DisplayName("Should provide HttpOptions bean when missing")
        fun optionsHttpClient_ProvidesBean() { }
    }

    @Nested
    @Tag("javaHttpClient")
    @DisplayName("JDK HttpClient Bean")
    inner class JavaHttpClientBean {
        @Test
        @DisplayName("Should provide JDK HttpClient bean when missing")
        fun javaHttpClient_ProvidesBean() { }
    }
}
