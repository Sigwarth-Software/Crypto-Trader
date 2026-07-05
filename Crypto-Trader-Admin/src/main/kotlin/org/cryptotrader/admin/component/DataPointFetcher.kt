package org.cryptotrader.admin.component

import org.cryptotrader.admin.model.ChartDataPoint
import org.cryptotrader.data.library.services.CurrencyService
import org.cryptotrader.universal.library.communication.response.TimeValueResponse
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class DataPointFetcher(
    private val currencyService: CurrencyService
) {
    fun getLastDayCurrencyHistory(currencyCode: String): List<ChartDataPoint<LocalDateTime, Double>> {
        val response: List<TimeValueResponse> = this.currencyService.getCurrencyHistory(currencyCode, 24, 300)
        val asChartDataPoint = response.map {
            ChartDataPoint(
                Instant.parse(it.timestamp).atZone(ZoneId.systemDefault())
                    .toLocalDateTime(),
                it.value
            )
        }
        return asChartDataPoint
    }
}