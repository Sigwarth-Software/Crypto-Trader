// TODO: Delete this file.
package org.cryptotrader.contact.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cryptotrader.contact.comm.email.request.EmailRequest
import org.cryptotrader.contact.comm.email.response.EmailResponse
import org.cryptotrader.contact.service.email.EmailService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

val log = KotlinLogging.logger {  }

@RestController
@RequestMapping("/contact/email")
@Deprecated(level = DeprecationLevel.WARNING, message = "This is an event-based API. A controller is not suitable here.")
class EmailController(
    private val emailService: EmailService
) {
    @GetMapping("/send")
    fun sendEmail(@RequestBody emailRequest: EmailRequest): ResponseEntity<EmailResponse> {
        log.info { "Sending email..." }
        try {
            this.emailService.send(emailRequest)
            log.info { "Email sent." }
        } catch (e: Exception) {
            log.error { "Error sending email: ${e.message}" }
            return ResponseEntity<EmailResponse>(EmailResponse(false), HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return ResponseEntity<EmailResponse>(EmailResponse(true), HttpStatus.OK)
    }
}