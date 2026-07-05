package org.cryptotrader.api.controller

import jakarta.annotation.security.PermitAll
import org.cryptotrader.console.library.communication.request.ConsoleCommandRequest
import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.console.library.component.ConsoleEventBinding
import org.cryptotrader.console.library.component.ConsoleRequestGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@RequestMapping("/api/console")
class ConsoleController @Autowired constructor(
    private val consoleRequestGateway: ConsoleRequestGateway
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ConsoleController::class.java)
    }

    @PermitAll
    @PostMapping("/execute")
    fun executeCommand(
        @RequestBody commandRequest: ConsoleCommandRequest,
        @RequestHeader(value = "Authorization", required = false) authorizationHeader: String?
    ): ResponseEntity<ConsoleCommandResponse> {
        log.info("Received console command: {}", commandRequest.commandText)
        try {
            val result: ConsoleCommandResponse = this.consoleRequestGateway.execute(
                ConsoleEventBinding.CONSOLE_REQUESTS,
                commandRequest,
                Duration.ofSeconds(15),
                authorizationHeader
            )
            return ResponseEntity.ok(result)
        } catch (exception: Exception) {
            log.error("Error executing console command: {}", commandRequest.commandText, exception)
            return ResponseEntity.badRequest().body(ConsoleCommandResponse("An error occurred: ${exception.message}", null))
        }
    }
}