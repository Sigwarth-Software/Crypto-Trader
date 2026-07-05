package org.cryptotrader.api.library.events

import org.cryptotrader.universal.library.events.model.EventBinding

enum class ApiEventBinding(override val bindingName: String) : EventBinding {
    USER_REGISTERED("userRegistered-out-0");
}
