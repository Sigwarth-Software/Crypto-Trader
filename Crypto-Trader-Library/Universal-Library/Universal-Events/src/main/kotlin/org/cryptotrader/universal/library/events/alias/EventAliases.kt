package org.cryptotrader.universal.library.events.alias

import org.springframework.messaging.Message
import java.util.function.Consumer

typealias MessageHandler<Response> = Consumer<Message<Response>>