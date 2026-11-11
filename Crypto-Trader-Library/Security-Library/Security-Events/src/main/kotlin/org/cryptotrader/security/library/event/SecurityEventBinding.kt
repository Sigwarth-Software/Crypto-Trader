package org.cryptotrader.security.library.event

import org.cryptotrader.universal.library.events.model.EventBinding

enum class SecurityEventBinding(override val bindingName: String) :
    EventBinding {
    USER_IP_DETECTION_REQUESTS("userIpDetection-out-0"),
    USER_IP_DETECTION_RESPONSES("userIpDetection-in-0");
}