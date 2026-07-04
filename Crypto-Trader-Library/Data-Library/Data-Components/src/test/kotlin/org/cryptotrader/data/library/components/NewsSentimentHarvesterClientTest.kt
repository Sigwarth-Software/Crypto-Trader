package org.cryptotrader.data.library.components

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.cryptotrader.data.library.component.NewsSentimentHarvesterClient
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mock

@Tag("NewsSentimentHarvesterClient")
@Tag("component")
@DisplayName("News Sentiment Harvester Client")
class NewsSentimentHarvesterClientTest : CryptoTraderTest() {

    @Mock
    lateinit var httpPost: HttpPost

    @Mock
    lateinit var objectMapper: ObjectMapper

    @Mock
    lateinit var httpClient: CloseableHttpClient

    private lateinit var client: NewsSentimentHarvesterClient

    @BeforeEach
    fun setUp() {
        this.client = NewsSentimentHarvesterClient(
            this.httpPost,
            this.objectMapper,
            this.httpClient,
            "https://localhost:8000"
        )
    }

    @Nested
    @Tag("triggerHarvest")
    @DisplayName("Trigger Harvest")
    inner class TriggerHarvest {
        @Test
        @DisplayName("Should trigger default harvest")
        fun triggerHarvest_TriggersDefault() { }

        @Test
        @DisplayName("Should trigger harvest with request payload")
        fun triggerHarvest_TriggersWithRequest() { }
    }

    @Nested
    @Tag("requestToJson")
    @DisplayName("Request To JSON")
    inner class RequestToJson {
        @Test
        @DisplayName("Should serialize request to JSON")
        fun requestToJson_Serializes() { }

        @Test
        @DisplayName("Should return null on serialization error")
        fun requestToJson_ReturnsNull_OnError() { }
    }

    @Nested
    @Tag("targetedHarvest")
    @DisplayName("Targeted Harvest")
    inner class TargetedHarvest {
        @Test
        @DisplayName("Should trigger targeted harvest with request")
        fun targetedHarvest_Triggers() { }

        @Test
        @DisplayName("Should create targeted request from dates")
        fun getTargetedHarvestRequest_CreatesFromDates() { }

        @Test
        @DisplayName("Should trigger targeted harvest with date range")
        fun triggerTargetedHarvest_TriggersWithDates() { }
    }

    @Nested
    @Tag("backFill")
    @DisplayName("Backfill")
    inner class Backfill {
        @Test
        @DisplayName("Should backfill monthly")
        fun backFillMonthly_Runs() { }

        @Test
        @DisplayName("Should backfill weekly")
        fun backFillWeekly_Runs() { }

        @Test
        @DisplayName("Should backfill daily")
        fun backFillDaily_Runs() { }
    }
}
