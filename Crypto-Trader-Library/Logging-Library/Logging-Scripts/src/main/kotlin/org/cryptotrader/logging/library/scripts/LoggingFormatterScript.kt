package org.cryptotrader.logging.library.scripts

import java.nio.charset.Charset
import java.util.*
import kotlin.math.min

fun humanSize(bytes: Int): String {
    if (bytes < 1024) {
        return bytes.toString() + "B"
    }
    val kb: Int = bytes / 1024
    if (kb < 1024) {
        return kb.toString() + "KB"
    }
    val mb: Int = kb / 1024
    return mb.toString() + "MB"
}

fun asText(payload: Any?, max: Int): String {
    if (payload is ByteArray) {
        val length = min(payload.size, max)
        var asString = String(payload, 0, length, Charset.defaultCharset())
        if (payload.size > max) asString += "…"
        return asString
    }
    val objectString = Objects.toString(payload, "")
    if (objectString.length > max) {
        return objectString.substring(0, max) + "…"
    }
    return objectString
}