package org.cryptotrader.contact.message.consumer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cryptotrader.api.library.events.UserRegisteredEvent
import org.cryptotrader.contact.comm.email.request.EmailRequest
import org.cryptotrader.contact.service.email.EmailService
import org.cryptotrader.contact.service.email.template.EmailTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

private val log = KotlinLogging.logger {  }

@Configuration
open class UserRegisteredConsumer @Autowired constructor(
    private val emailService: EmailService
) {
    
    @Bean
    open fun userRegistered(): Consumer<UserRegisteredEvent> {
        log.info { "User registered consumer." }
        return Consumer { event ->
            log.info { "User registered: ${event.dateTime}" }
            // TODO: Temporary. Remove this after testing.
            val emailRequest = EmailRequest(
                to = "test@sscryptotrader.com",
                subject = "Welcome to Crypto Trader",
                body = "",
                emailTemplate = EmailTemplate.WELCOME
            )
            this.emailService.send(emailRequest)
        }
    }
}