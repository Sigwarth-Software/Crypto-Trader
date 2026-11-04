package org.cryptotrader.health.library.event

import org.cryptotrader.universal.library.events.model.EventBinding

enum class HealthEventBinding(override val bindingName: String) : EventBinding {
    HEALTH_STATUS_REQUESTS("healthStatusRequests-out-0"),
    HEALTH_STATUS_RESPONSES("healthStatusResponses-out-0")
}
