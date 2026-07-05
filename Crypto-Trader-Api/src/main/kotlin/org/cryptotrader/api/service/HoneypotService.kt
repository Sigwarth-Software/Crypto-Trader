package org.cryptotrader.api.service

import jakarta.servlet.http.HttpServletRequest
import org.cryptotrader.security.library.model.BanType
import org.cryptotrader.security.library.service.IpBanService
import org.cryptotrader.security.library.service.model.IpBanPolicy
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class HoneypotService(
    private val ipBanService: IpBanService,
    @param:Value("\${security.honeypot.paths:}")
    private val configuredPaths: String = ""
) {
    private val suffixVariants = listOf(
        ".bak", ".backup", ".old", ".save", ".sample", ".example", ".dist",
        ".txt", ".json", ".yaml", ".yml", ".ini", ".php", ".dev", ".local", ".prod"
    )

    private val honeypotPaths: Set<String> = configuredPaths.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { normalizePath(it) }
        .toSet()

    fun captureHoneypot(request: HttpServletRequest?): ResponseEntity<Void>? {
        val path = normalizePath(request?.requestURI ?: "")
        val query = (request?.queryString ?: "").lowercase()

        if (this.isViolation(path, query)) {
            val clientIp = extractClientIp(request)
            if (clientIp.isNotBlank() && !IpBanPolicy.shouldBypass(clientIp)) {
                this.ipBanService.ban(clientIp, BanType.PERMA)
            }
            return ResponseEntity.notFound().build()
        }
        return null
    }

    private fun isViolation(path: String, query: String): Boolean =
        this.isHoneypotPath(path) || this.looksLikeSecretProbe(path, query)

    private fun isHoneypotPath(path: String): Boolean {
        for (honeypotPath in this.honeypotPaths) {
            if (path == honeypotPath) {
                return true
            }
            if (path.startsWith("$honeypotPath/")) {
                return true
            }
            if (this.suffixVariants.any { suffix -> path == honeypotPath + suffix }) {
                return true
            }
        }
        return false
    }

    private fun looksLikeSecretProbe(uri: String, query: String): Boolean {
        val keywords = listOf(
            ".env", "dotenv", "aws_access_key_id", "aws_secret_access_key",
            "google_application_credentials", "gcp_credentials", "db_password", "db_user",
            "api_key", "apikey", "secret_key", "secret", "access_token", "private_key",
            "ssh", "credentials", "config.php", "wp-config", "adminer"
        )
        return keywords.any { kw ->
            uri.contains(kw) || query.contains(kw)
        }
    }

    private fun extractClientIp(request: HttpServletRequest?): String {
        if (request == null) return ""
        val headerNames = listOf(
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",
            "X-Client-IP",
            "Fastly-Client-Ip",
            "True-Client-Ip",
            "X-Cluster-Client-Ip",
            "X-Forwarded",
            "Forwarded-For",
            "Forwarded"
        )
        for (name in headerNames) {
            val raw = request.getHeader(name)
            if (!raw.isNullOrBlank()) {
                val first = raw.split(',').first().trim()
                if (first.isNotBlank()) return first
            }
        }
        return request.remoteAddr ?: ""
    }

    private fun normalizePath(raw: String): String {
        if (raw.isBlank()) return "/"
        val lower = raw.lowercase()
        return if (lower.startsWith("/")) lower else "/$lower"
    }
}
