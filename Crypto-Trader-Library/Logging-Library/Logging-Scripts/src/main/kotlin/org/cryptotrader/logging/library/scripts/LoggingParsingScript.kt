package org.cryptotrader.logging.library.scripts

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.logging.library.communication.request.FrontendLogRequest
import org.cryptotrader.logging.library.events.FrontendLogEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.lang.Nullable
import org.springframework.util.StringUtils
import java.io.BufferedReader
import java.io.StringReader
import java.nio.charset.Charset
import java.util.*

private val log: Logger = LoggerFactory.getLogger("LoggingParsingScript")

fun isJsonContentType(@Nullable contentType: String?): Boolean {
    if (!StringUtils.hasText(contentType)) {
        return false
    }

    try {
        val mediaType: MediaType = MediaType.parseMediaType(contentType!!)
        val subtype: String = mediaType.subtype.lowercase(Locale.getDefault())
        return "json" == subtype || subtype.endsWith("+json")
    } catch (ignored: IllegalArgumentException) {
        return contentType!!.lowercase(Locale.getDefault()).contains("json")
    }
}

fun sizeOf(@Nullable payload: Any?): Int {
    if (payload == null) {
        return 0
    }
    if (payload is ByteArray) {
        return payload.size
    }
    val asString: String = payload.toString()
    return asString.toByteArray(Charset.defaultCharset()).size
}

fun parseNdjson(
    ndjsonBody: String,
    request: HttpServletRequest,
    user: ProductUser?
): MutableList<FrontendLogEvent?> {
    val events: MutableList<FrontendLogEvent?> =
        ArrayList<FrontendLogEvent?>()

    try {
        BufferedReader(StringReader(ndjsonBody)).use { reader ->
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                if (line!!.isNotBlank()) {
                    val dto: FrontendLogRequest? =
                        ObjectMapper().readValue(
                            line,
                            FrontendLogRequest::class.java
                        )
                    if (dto != null) {
                        events.add(mapToEvent(dto, request, user))
                    }
                }
            }
        }
    } catch (exception: Exception) {
        log.error(
            "Failed to parse NDJSON frontend log batch",
            exception
        )
    }
    return events
}