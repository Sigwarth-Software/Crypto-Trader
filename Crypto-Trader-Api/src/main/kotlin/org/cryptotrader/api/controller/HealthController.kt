package org.cryptotrader.api.controller

import jakarta.annotation.security.PermitAll
import org.cryptotrader.health.library.communication.request.HealthStatusRequest
import org.cryptotrader.health.library.communication.response.HealthStatusResponse
import org.cryptotrader.health.library.event.HealthEventBinding
import org.cryptotrader.health.library.event.HealthStatusRequestGateway
import org.cryptotrader.health.library.model.CryptoTraderService
import org.cryptotrader.health.library.model.HealthStatusTimeRange
import org.cryptotrader.health.library.model.ServiceStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@PermitAll
@RestController
@RequestMapping("/api/health")
class HealthController @Autowired constructor(
    private val healthStatusRequestGateway: HealthStatusRequestGateway
) {
    @GetMapping("/{moduleName}/{timeRange}")
    fun get(
        @PathVariable("moduleName") moduleName: String,
        @PathVariable("timeRange") timeRange: String
    ): ResponseEntity<List<ServiceStatus>> {
        if (moduleName.isBlank() || timeRange.isBlank()) {
            return ResponseEntity.badRequest().build()
        }

        val service: CryptoTraderService
        val range: HealthStatusTimeRange

        try {
            service = CryptoTraderService.from(moduleName)
            range = HealthStatusTimeRange.from(timeRange)

            return try {
                val request = HealthStatusRequest(service, range)
                val results: HealthStatusResponse =
                    this.healthStatusRequestGateway.execute(
                        HealthEventBinding.HEALTH_STATUS_REQUESTS,
                        request
                    )

                if (results.healthStatuses.isEmpty()) {
                    return ResponseEntity.noContent().build()
                }
                ResponseEntity.ok(results.healthStatuses)
            } catch (_: Exception) {
                return ResponseEntity.internalServerError().build()
            }
        } catch (_: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
    }
}
