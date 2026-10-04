package org.cryptotrader.api.library.scripts.http

import org.springframework.http.ResponseCookie
import java.time.Duration
import java.time.Instant

/**
 * Build a refresh cookie with optional environment-specific flags.
 *
 * @param name cookie name (e.g., __Host-rt)
 * @param value opaque token id value
 * @param expiresAt absolute expiry used to compute Max-Age
 * @param secure whether to mark Secure (default true)
 * @param sameSite SameSite policy (default Strict)
 * @return a ResponseCookie ready to be added to Set-Cookie
 */
fun buildRefreshCookie(
    name: String,
    value: String,
    expiresAt: Instant,
    secure: Boolean = true,
    sameSite: String = "Strict"
): ResponseCookie {
    val maxAge: Long = Duration.between(Instant.now(), expiresAt).seconds.coerceAtLeast(0)
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite(sameSite)
        .path("/")
        .maxAge(maxAge)
        .build()
}

/**
 * Create a deletion cookie (Max-Age=0) with optional flags.
 *
 * @param name cookie name to clear
 * @param secure whether to mark Secure (default true)
 * @param sameSite SameSite policy (default Strict)
 * @return a ResponseCookie that instructs the browser to delete the cookie
 */
fun deleteCookie(
    name: String,
    secure: Boolean = true,
    sameSite: String = "Strict"
): ResponseCookie =
    ResponseCookie.from(name, "")
        .httpOnly(true)
        .secure(secure)
        .sameSite(sameSite)
        .path("/")
        .maxAge(0)
        .build()