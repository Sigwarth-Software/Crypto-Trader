package org.cryptotrader.api.controller

import jakarta.annotation.security.PermitAll
import org.cryptotrader.health.library.model.CryptoTraderService
import org.cryptotrader.health.library.model.HealthStatusTimeRange
import org.cryptotrader.health.library.model.ServiceStatus
import org.cryptotrader.health.library.service.HealthStatusService
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
    private val healthStatusService: HealthStatusService
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
        } catch (_: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
        val results: List<ServiceStatus> =
            this.healthStatusService.getFromTimeRange(service, range)

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build()
        }

        return ResponseEntity.ok(results)
    }
}
