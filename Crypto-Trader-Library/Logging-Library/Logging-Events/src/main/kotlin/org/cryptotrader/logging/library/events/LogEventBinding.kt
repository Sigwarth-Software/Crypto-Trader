package org.cryptotrader.logging.library.events

import org.cryptotrader.universal.library.events.model.EventBinding

enum class LogEventBinding(override val bindingName: String) : EventBinding {
    FRONTEND_LOGS_REQUESTS("frontendLogs-out-0"),
    EXECUTION_SPEED_LOGS_REQUESTS("executionSpeedLogs-out-0");
}
