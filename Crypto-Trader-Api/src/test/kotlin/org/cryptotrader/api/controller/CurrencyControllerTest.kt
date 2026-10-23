package org.cryptotrader.api.controller

import org.cryptotrader.api.library.communication.request.AssetValueRequest
import org.cryptotrader.api.library.communication.response.AssetValueResponse
import org.cryptotrader.api.library.communication.response.DisplayCurrencyListResponse
import org.cryptotrader.api.library.communication.response.DisplayCurrencyResponse
import org.cryptotrader.api.library.communication.response.PerformanceRatingResponse
import org.cryptotrader.data.library.entity.currency.Currency
import org.cryptotrader.data.library.model.currency.PerformanceRating
import org.cryptotrader.data.library.services.CurrencyService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.universal.library.communication.response.TimeValueResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@Tag("controller")
@DisplayName("CurrencyController")
class CurrencyControllerTest : CryptoTraderTest() {
    @Mock
    lateinit var currencyService: CurrencyService

    @InjectMocks
    private lateinit var controller: CurrencyController

    val testCode = "TEST"
    val testCurrencyName = "Test Currency"
    val testCurrency: Currency = Currency.builder()
        .currencyCode(this.testCode)
        .value(10.0)
        .name(this.testCurrencyName)
        .urlPath("test")
        .build()

    @Nested
    @DisplayName("getCurrencyValue")
    inner class GetCurrencyValue {
        @Nested
        @DisplayName("when the request is valid")
        inner class WhenValidRequest {
            @Test
            @DisplayName("should calculate asset value")
            fun calculateAssetValue() {
                val request = AssetValueRequest(testCode, 1.0)
                `when`(currencyService.getCurrencyByCurrencyCode(request.currencyCode)).thenReturn(
                    testCurrency
                )
                val response: ResponseEntity<AssetValueResponse> =
                    controller.getCurrencyValue(request)
                assertEquals(HttpStatus.OK, response.statusCode)
                assertEquals(10.0, response.body!!.value)
            }
        }

        @Nested
        @DisplayName("when the currency is missing")
        inner class WhenCurrencyIsMissing {
            @Test
            @DisplayName("should return not found")
            fun returnNotFound() {
                val request = AssetValueRequest("MISSING", 1.0)
                `when`(currencyService.getCurrencyByCurrencyCode(request.currencyCode)).thenReturn(
                    null
                )
                val response: ResponseEntity<AssetValueResponse> =
                    controller.getCurrencyValue(request)
                assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
                assertEquals(null, response.body)
            }
        }
    }

    @Nested
    @DisplayName("getCurrencyPerformance")
    inner class GetCurrencyPerformance {

        @Nested
        @DisplayName("when the request is valid")
        inner class WhenRequestValid {
            @BeforeEach
            fun setUp() {
                `when`(currencyService.getDayPerformance(testCode)).thenReturn(
                    PerformanceRating.UP
                )
                `when`(currencyService.getPercentageDayPerformance(testCode)).thenReturn(
                    "+%5.0"
                )
            }

            @Test
            @DisplayName("should return performance and percent change for valid request")
            fun getCurrencyPerformance_ReturnsPerformance_ValidRequest() {
                val response: ResponseEntity<PerformanceRatingResponse> =
                    controller.getCurrencyPerformance(testCode)
                assertEquals(HttpStatus.OK, response.statusCode)
                assertEquals(
                    PerformanceRatingResponse(
                        PerformanceRating.UP,
                        "+%5.0"
                    ), response.body
                )
            }
        }

        @Nested
        @DisplayName("when code is invalid")
        inner class WhenCodeIsInvalid {
            val invalidCode = "INVALID"

            @BeforeEach
            fun setUp() {
                `when`(currencyService.getDayPerformance(invalidCode)).thenReturn(
                    null
                )
            }

            @Test
            @DisplayName("Should return bad request when code invalid")
            fun getCurrencyPerformance_ReturnsBadRequest_OnInvalidCode() {
                val response: ResponseEntity<PerformanceRatingResponse> =
                    controller.getCurrencyPerformance(invalidCode)
                assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
                assertEquals(null, response.body)
            }
        }
    }

    @Nested
    @Tag("getAll")
    @DisplayName("Get All Currencies")
    inner class GetAll {
        lateinit var testDisplayCurrencyResponse: DisplayCurrencyListResponse
        lateinit var testDisplayCurrencyResponses: List<DisplayCurrencyResponse>

        @Test
        @DisplayName("should return all currencies")
        fun returnAllCurrencies() {
            testDisplayCurrencyResponse = DisplayCurrencyListResponse(
                listOf(
                    DisplayCurrencyResponse("Test Currency", "TEST", 10.0, "test"),
                    DisplayCurrencyResponse("Another Currency", "ANOTHER", 20.0, "another")
                )
            )
            `when`(currencyService.getCurrencyValuesResponse()).thenReturn(testDisplayCurrencyResponse)
            val response: ResponseEntity<DisplayCurrencyListResponse> = controller.all
            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(testDisplayCurrencyResponse, response.body)
        }

        @Test
        @DisplayName("should return all currencies with offset")
        fun returnAllCurrenciesWithOffset() {
            testDisplayCurrencyResponses = (1..200).map { i ->
                DisplayCurrencyResponse("Test Currency $i", "TEST$i", 10.0 + i)
            }
            val testDisplayCurrencyResponse = DisplayCurrencyListResponse(testDisplayCurrencyResponses)
            val expectedResponses: List<DisplayCurrencyResponse> = testDisplayCurrencyResponses.subList(10, 20)
            val expectedResponse = DisplayCurrencyListResponse(expectedResponses)
            val testOffset = 10
            `when`(currencyService.getCurrencyValuesResponse(testOffset)).thenReturn(expectedResponse)
            val response: ResponseEntity<DisplayCurrencyListResponse> = controller.getAllWithOffset(testOffset)
            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(expectedResponse, response.body)
        }

        @Nested
        @DisplayName("when offset negative")
        inner class WhenOffsetNegative {
            val negativeOffset = -1

            @Test
            @DisplayName("should return bad request")
            fun returnBadRequest() {
                val response: ResponseEntity<DisplayCurrencyListResponse> =
                    controller.getAllWithOffset(negativeOffset)
                assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
                assertEquals(null, response.body)
            }
        }
    }

    @Nested
    @DisplayName("getHistory")
    inner class GetHistory {
        @DisplayName("when history is empty")
        @Nested
        inner class WhenHistoryIsEmpty {
            val hours = 1
            val intervalSeconds = 60
            val expectedSize = hours * 60 / intervalSeconds

            @BeforeEach
            fun setUp() {
                `when`(
                    currencyService.getCurrencyHistory(
                        testCode,
                        hours,
                        intervalSeconds
                    )
                ).thenReturn(List(expectedSize) { TimeValueResponse() })
            }

            @Test
            @DisplayName("should return history list or no content")
            fun returnHistoryListOrNoContent() {
                val response: ResponseEntity<List<TimeValueResponse>> =
                    controller.getHistory(testCode, hours, intervalSeconds)
                assertEquals(HttpStatus.OK, response.statusCode)
                assertEquals(expectedSize, response.body!!.size)

                `when`(
                    currencyService.getCurrencyHistory(
                        testCode,
                        hours,
                        intervalSeconds
                    )
                )
                    .thenReturn(emptyList())
                val noContentResponse: ResponseEntity<List<TimeValueResponse>> =
                    controller.getHistory(testCode, hours, intervalSeconds)
                assertEquals(
                    HttpStatus.NO_CONTENT,
                    noContentResponse.statusCode
                )
                assertEquals(null, noContentResponse.body)
            }
        }
    }
}
