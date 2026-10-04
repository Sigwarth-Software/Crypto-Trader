package org.cryptotrader.api.library.services.dpop

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.cryptotrader.api.library.model.dpop.DpopVerificationResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestMethod
import java.math.BigInteger
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.*
import java.time.Instant
import java.util.*
import kotlin.math.abs

/**
 * Cryptographic DPoP verifier using Auth0 JWT library + JCA keys built from the embedded JWK.
 * - Verifies the JWS signature against the public key in header.jwk
 * - Validates htm/htu canonicalization, and iat skew window
 * - Optionally validates 'ath' claim if an access token is provided
 * - Computes RFC7638 JWK thumbprint (jkt)
 *
 * NOTE: This implementation supports RSA (RS256) and EC (ES256/384/512) algorithms.
 */
@Service
class DpopVerifierService(
    @Value("\${security.auth.dpop.allowed-algs:ES256}") private val allowedAlgos: String
) {
    private val allowedAlgSet: Set<String> = this.allowedAlgos.split(",")
        .map { it.trim().uppercase(Locale.ROOT) }
        .filter { it.isNotEmpty() }
        .toSet()

    fun verify(
        dpopJwt: String?,
        requestMethod: RequestMethod,
        requestUri: String,
        accessTokenForAth: String? = null,
        skewToleranceSeconds: Long = 20
    ): DpopVerificationResult? {
        return this.verify(
            dpopJwt,
            requestMethod.name,
            requestUri,
            accessTokenForAth,
            skewToleranceSeconds
        )
    }

    /**
     * Verify a DPoP proof and extract its key fingerprint and nonce.
     *
     * In simple terms: check that the proof was really signed by the key in its header, that it refers to
     * this exact HTTP request (method + URL), that it is fresh in time, and that its random id is unique.
     * If an access token is provided, also check that the proof references that exact token (ath claim).
     *
     * @param dpopJwt the DPoP header value (compact JWS)
     * @param requestMethod the HTTP method we expect (e.g., GET/POST)
     * @param requestUri the absolute URL of this request
     * @param accessTokenForAth optional access token string used to validate the 'ath' claim
     * @param skewToleranceSeconds allowed clock drift for iat (default ~20s)
     * @return VerificationResult with jkt (key fingerprint) and jti (one-time nonce) or null if invalid
     */
    fun verify(
        dpopJwt: String?,
        requestMethod: String,
        requestUri: String,
        accessTokenForAth: String? = null,
        skewToleranceSeconds: Long = 20
    ): DpopVerificationResult? {
        if (dpopJwt.isNullOrBlank()) return null
        try {
            val decoded: DecodedJWT = JWT.decode(dpopJwt)
            val header: String = decoded.header
            val jwtAlgorithm: String = decoded.algorithm ?: return null
            if (!this.isAllowedAlgorithm(jwtAlgorithm)) {
                log.debug("DPoP alg not allowed: {}", jwtAlgorithm)
                return null
            }
            val jwkMap: Map<String, Any> = decoded.getHeaderClaim("jwk").asMap() ?: return null
            val publicKey: PublicKey = buildPublicKeyFromJwk(jwkMap) ?: return null
            val verifier: JWTVerifier = generateVerifierByAlgorithm(jwtAlgorithm, publicKey) ?: return null

            // Verify JWS signature first
            val verified: DecodedJWT = verifier.verify(dpopJwt)

            // Enforce typ=dpop+jwt per spec
            val type: String? = decoded.getHeaderClaim("typ").asString()
            if (!isJwtType(type)) {
                log.debug("DPoP typ header not dpop+jwt: {}", type)
                return null
            }

            // Claim checks
            val method: String = verified.getClaim("htm").asString() ?: return null
            val httpTokenUri: String = verified.getClaim("htu").asString() ?: return null
            val issuedAt: Long = verified.getClaim("iat").asLong() ?: return null
            if (method != requestMethod) {
                return null
            }
            val normalizedTokenUri: String = this.normalizeUri(httpTokenUri)
            val normalizedRequest: String = this.normalizeUri(requestUri)
            if (normalizedTokenUri != normalizedRequest) {
                return null
            }
            val now: Long = Instant.now().epochSecond
            val isOutsideSkewTolerance: Boolean = abs(now - issuedAt) > skewToleranceSeconds
            if (isOutsideSkewTolerance) {
                return null
            }

            // Optional ath (hash of access token)
            val accessTokenHash: String? = verified.getClaim("ath").asString()
            if (accessTokenForAth != null) {
                val expectedTokenHash: String = this.computeExpectedAth(accessTokenForAth)
                if (!isTokenMatch(accessTokenHash, expectedTokenHash)) {
                    log.debug("ath mismatch: expected={}, got={}", expectedTokenHash, accessTokenHash)
                    return null
                }
            }

            val jwtId: String? = verified.getClaim("jti").asString()
            val jwtThumbprint: String = this.computeJwkThumbprint(jwkMap) ?: return null
            return DpopVerificationResult(jwtThumbprint, jwtId)
        } catch (ex: Exception) {
            log.debug("DPoP verification failed", ex)
            return null
        }
    }

    private fun isTokenMatch(accessTokenHash: String?, expectedTokenHash: String): Boolean =
        accessTokenHash != null && accessTokenHash == expectedTokenHash

    private fun isJwtType(type: String?): Boolean =
        type != null && type.lowercase(Locale.ROOT) == "dpop+jwt"

    private fun generateVerifierByAlgorithm(algorithm: String, key: PublicKey): JWTVerifier? {
        return when (algorithm.uppercase(Locale.ROOT)) {
            "RS256" -> JWT.require(Algorithm.RSA256(key as RSAPublicKey, null)).build()
            "RS384" -> JWT.require(Algorithm.RSA384(key as RSAPublicKey, null)).build()
            "RS512" -> JWT.require(Algorithm.RSA512(key as RSAPublicKey, null)).build()
            "ES256" -> JWT.require(Algorithm.ECDSA256(key as ECPublicKey, null)).build()
            "ES384" -> JWT.require(Algorithm.ECDSA384(key as ECPublicKey, null)).build()
            "ES512" -> JWT.require(Algorithm.ECDSA512(key as ECPublicKey, null)).build()
            else -> null
        }
    }

    private fun buildPublicKeyFromJwk(jsonWebKey: Map<String, Any>): PublicKey? {
        val keyType: String = jsonWebKey["kty"]?.toString() ?: return null
        return when (keyType.uppercase(Locale.ROOT)) {
            "RSA" -> this.buildRsaPublicKey(jsonWebKey)
            "EC" -> this.buildEcPublicKey(jsonWebKey)
            else -> null
        }
    }

    private fun buildRsaPublicKey(jsonWebKey: Map<String, Any>): PublicKey? {
        val nB64: String = jsonWebKey["n"]?.toString() ?: return null
        val eB64: String = jsonWebKey["e"]?.toString() ?: return null
        val modulus = BigInteger(1, base64UrlDecode(nB64))
        val publicExponent = BigInteger(1, base64UrlDecode(eB64))
        val keySpec = RSAPublicKeySpec(modulus, publicExponent)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    private fun buildEcPublicKey(jsonWebKey: Map<String, Any>): PublicKey? {
        val curve = jsonWebKey["crv"]?.toString() ?: return null
        val xBytes = base64UrlDecode(jsonWebKey["x"]?.toString() ?: return null)
        val yBytes = base64UrlDecode(jsonWebKey["y"]?.toString() ?: return null)
        val x = BigInteger(1, xBytes)
        val y = BigInteger(1, yBytes)
        val ecSpec: ECParameterSpec = ecSpecForCurve(curve) ?: return null
        val point = ECPoint(x, y)
        val pubSpec = ECPublicKeySpec(point, ecSpec)
        val keyFactory: KeyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(pubSpec)
    }

    private fun ecSpecForCurve(crv: String): ECParameterSpec? {
        val name = when (crv) {
            "P-256" -> "secp256r1"
            "P-384" -> "secp384r1"
            "P-521" -> "secp521r1"
            else -> return null
        }
        val params: AlgorithmParameters = AlgorithmParameters.getInstance("EC").apply {
            init(ECGenParameterSpec(name))
        }
        return params.getParameterSpec(ECParameterSpec::class.java)
    }

    private fun normalizeUri(urlString: String): String {
        val uri = URI(urlString)
        val scheme: String? = uri.scheme?.lowercase(Locale.ROOT)
        val host: String? = uri.host?.lowercase(Locale.ROOT)
        val portPart: String = if (this.hasDifferentPort(uri, scheme)) {
            ":${uri.port}"
        } else ""
        val path: String = uri.path ?: "/"
        val query: String = if (!uri.query.isNullOrBlank()) {
            "?${uri.query}"
        } else ""
        return "$scheme://$host$portPart$path$query"
    }

    private fun hasDifferentPort(uri: URI, scheme: String?): Boolean =
        uri.port != -1 && uri.port != this.defaultPort(scheme)

    private fun defaultPort(scheme: String?): Int =
        when (scheme?.lowercase(Locale.ROOT)) {
            "http" -> 80
            "https" -> 443
            else -> -1
        }

    private fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    private fun base64UrlEncode(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private fun base64UrlDecode(urlEncodedData: String): ByteArray =
        Base64.getUrlDecoder().decode(urlEncodedData)

    private fun computeExpectedAth(accessToken: String): String {
        val tokenBytes = accessToken.toByteArray(StandardCharsets.UTF_8)
        val digest = this.sha256(tokenBytes)
        return this.base64UrlEncode(digest)
    }

    private fun computeJwkThumbprint(jsonWebKey: Map<String, Any>): String? {
        val keyType = jsonWebKey["kty"]?.toString() ?: return null
        val canonicalJson: String = when (keyType.uppercase(Locale.ROOT)) {
            "RSA" -> this.canonicalizeJson(mapOf(
                "e" to (jsonWebKey["e"]?.toString() ?: return null),
                "kty" to "RSA",
                "n" to (jsonWebKey["n"]?.toString() ?: return null)
            ))
            "EC" -> this.canonicalizeJson(mapOf(
                "crv" to (jsonWebKey["crv"]?.toString() ?: return null),
                "kty" to "EC",
                "x" to (jsonWebKey["x"]?.toString() ?: return null),
                "y" to (jsonWebKey["y"]?.toString() ?: return null)
            ))
            else -> return null
        }
        val jsonBytes = canonicalJson.toByteArray(StandardCharsets.UTF_8)
        val digest: ByteArray = this.sha256(jsonBytes)
        return this.base64UrlEncode(digest)
    }

    private fun canonicalizeJson(map: Map<String, String>): String {
        val entries = map.toSortedMap().entries
        val jsonString = StringBuilder().also {
            it.append('{')
        }
        var first = true
        for ((key, value) in entries) {
            if (!first) jsonString.append(',') else first = false
            this.appendJsonKeyValue(jsonString, key, value)
        }
        jsonString.append('}')
        return jsonString.toString()
    }

    private fun appendJsonKeyValue(jsonString: StringBuilder,
                                   key: String,
                                   value: String) {
        jsonString.append('"').append(this.escapeJson(key))
            .append('"')
            .append(':')
        jsonString.append('"').append(this.escapeJson(value))
            .append('"')
    }

    private fun escapeJson(inputJson: String): String =
        buildString(inputJson.length) {
            for (jsonChar in inputJson) when (jsonChar) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(jsonChar)
            }
        }

    fun isValidProof(dpopProof: String?): Boolean {
        return dpopProof != null && dpopProof.isNotBlank()
    }

    private fun isAllowedAlgorithm(algorithms: String): Boolean {
        val algorithmSet: Set<String> = this.allowedAlgos.split(",").map {
            it.trim().uppercase(Locale.ROOT)
        }.filter { it.isNotEmpty() }.toSet()
        return algorithmSet.contains(algorithms.uppercase(Locale.ROOT))
    }

    companion object {
        private val log = LoggerFactory.getLogger(DpopVerifierService::class.java)
    }
}