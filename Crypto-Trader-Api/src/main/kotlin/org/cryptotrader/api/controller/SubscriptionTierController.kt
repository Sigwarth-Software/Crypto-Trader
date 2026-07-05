package org.cryptotrader.api.controller

import jakarta.annotation.security.PermitAll
import org.cryptotrader.api.library.communication.response.SubscriptionTierPricesResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/subscription-tier")
@RestController
class SubscriptionTierController {
    @PermitAll
    @GetMapping("/prices")
    fun getCosts(): ResponseEntity<SubscriptionTierPricesResponse> {
        // TODO: Replace hardcoded values with actual database entries.
        return ResponseEntity.ok(SubscriptionTierPricesResponse(0.0, 25.0, 50.0))
    }
}