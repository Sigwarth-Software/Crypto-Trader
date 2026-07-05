package org.cryptotrader.console.library.component

import org.cryptotrader.universal.library.events.model.EventBinding

enum class ConsoleEventBinding(override val bindingName: String) : EventBinding {
    CONSOLE_REQUESTS("consoleRequests-out-0"),
    CONSOLE_RESPONSES("consoleResponses-out-0");
}
