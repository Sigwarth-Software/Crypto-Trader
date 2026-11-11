package org.cryptotrader.agent

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

/**
 * Missing static resources are normal 404s, including OAuth discovery paths
 * probed by MCP clients when the server does not expose OAuth metadata.
 * Handle these before the shared catch-all advice logs them as application
 * errors.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class McpNotFoundExceptionHandler {
    @ExceptionHandler
    fun handleNoResourceFound(exception: NoResourceFoundException): ResponseEntity<Void> =
        ResponseEntity.notFound().build()
}
