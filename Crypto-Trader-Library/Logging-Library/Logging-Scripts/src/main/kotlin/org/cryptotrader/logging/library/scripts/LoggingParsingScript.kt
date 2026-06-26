package org.cryptotrader.logging.library.scripts

import org.springframework.http.MediaType
import org.springframework.lang.Nullable
import org.springframework.util.StringUtils
import java.nio.charset.Charset
import java.util.*

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
