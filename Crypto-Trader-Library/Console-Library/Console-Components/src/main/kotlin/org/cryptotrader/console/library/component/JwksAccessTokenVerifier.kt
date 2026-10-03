package org.cryptotrader.console.library.component

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Verification
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.cryptotrader.api.library.model.jwt.JwtClaims
import org.cryptotrader.console.library.component.models.AccessTokenVerifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Duration
import java.util.*

/**
 * Verifies JWT access tokens by resolving the signing key from the API's JWKS endpoint.
 * Keeps a simple in-memory cache of kid -> RSAPublicKey and refreshes on cache miss.
 */
@Component
class JwksAccessTokenVerifier(
    @param:Value("\${security.jwt.jwks-uri:http://localhost:8088/.well-known/jwks.json}")
    private val jwksUri: String,
    @param:Value("\${security.jwt.issuer:https://api.cryptotrader.com}")
    private val issuer: String,
    @param:Value("\${security.jwt.issuers:}")
    private val issuersCsv: String,
    @param:Value("\${security.jwt.audience:crypto-trader-api}")
    private val audienceCsv: String
) : AccessTokenVerifier {

    private val log: Logger = LoggerFactory.getLogger(JwksAccessTokenVerifier::class.java)
    private val objectMapper = ObjectMapper()
    private val httpClient: HttpClient = HttpClient.newBuilder()
                                                   .connectTimeout(Duration.ofSeconds(5))
                                                   .build()

    private val keyCache: MutableMap<String, RSAPublicKey> = Collections.synchronizedMap(HashMap())

    private val audiences: List<String> = this.audienceCsv.split(",").map {
        it.trim() 
    }.filter { 
        it.isNotBlank() 
    }

    override fun validateAndParse(token: String): JwtClaims {
        val decoded: DecodedJWT = JWT.decode(token)
        val kid: String? = decoded.keyId
        if (kid.isNullOrBlank()) {
            refreshKeys()
            val keys: List<RSAPublicKey> = synchronized(this.keyCache) { 
                this.keyCache.values.toList() 
            }
            var lastError: Exception? = null
            for (pub in keys) {
                try {
                    return verifyWithKey(token, pub)
                } catch (ex: Exception) {
                    lastError = ex as? Exception ?: RuntimeException(ex)
                }
            }
            throw IllegalArgumentException("JWT is missing 'kid' header and no JWKS key verified signature", lastError)
        }
        val publicKey: RSAPublicKey = resolveKey(kid)
        return verifyWithKey(token, publicKey)
    }

    private fun verifyWithKey(token: String, publicKey: RSAPublicKey): JwtClaims {
        val algorithm: Algorithm = Algorithm.RSA256(publicKey, null)
        val verifierBuilder: Verification = JWT.require(algorithm)
        val allowedIss: Array<String> = this.getAllowedIssuers()
        if (allowedIss.isNotEmpty()) {
            verifierBuilder.withIssuer(*allowedIss)
        }
        if (this.audiences.isNotEmpty()) {
            verifierBuilder.withAudience(*this.audiences.toTypedArray())
        }
        val verifiedJwt: DecodedJWT = verifierBuilder.build().verify(token)
        val subject: String? = verifiedJwt.subject
        val email: String? = verifiedJwt.getClaim("email").asString()
        val issuedAt: Date? = verifiedJwt.issuedAt
        val expiresAt: Date? = verifiedJwt.expiresAt
        val confirmationMap: Map<String?, Any?>? = verifiedJwt.getClaim("cnf")?.asMap()
        val jwkThumbprint: String? = confirmationMap?.get("jkt")?.toString()
        return JwtClaims(subject, email, issuedAt, expiresAt, jwkThumbprint)
    }

    private fun getAllowedIssuers(): Array<String> {
        val issuersList: List<String> = if (this.issuersCsv.isNotBlank()) {
            this.issuersCsv.split(",")
        } else listOf(this.issuer)
        return issuersList.map {
            it.trim() 
        }.filter { 
            it.isNotEmpty() 
        }.toTypedArray()
    }

    private fun resolveKey(kid: String): RSAPublicKey {
        val cachedKey: RSAPublicKey? = this.keyCache[kid]
        if (cachedKey != null) {
            return cachedKey
        }
        this.refreshKeys()
        return this.keyCache[kid]
            ?: throw IllegalStateException("Signing key with not found in JWKS at ${this.jwksUri}")
    }

    private fun getJwksResponse(): HttpResponse<String> {
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(this.jwksUri))
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build()
        val response: HttpResponse<String> = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("Failed to fetch JWKS (${this.jwksUri}): HTTP ${'$'}{response.statusCode()}")
        }
        return response
    }
    
    @Synchronized
    private fun refreshKeys() {
        val maxAttempts = 2
        var attempt = 0
        var lastEx: Exception? = null
        while (attempt < maxAttempts) {
            attempt++
            try {
                val response: HttpResponse<String> = getJwksResponse()
                val node: JsonNode = this.objectMapper.readTree(response.body())
                val keys: JsonNode = node.get("keys") ?: return
                val rsaKeyMap = HashMap<String, RSAPublicKey>()
                for (jwk in keys) {
                    val keyType: String? = jwk.get("kty")?.asText()
                    val keyUsage: String? = jwk.get("use")?.asText()
                    val keyAlgorithm: String? = jwk.get("alg")?.asText()
                    val kid: String? = jwk.get("kid")?.asText()
                    if (keyType != "RSA" || keyUsage != "sig" || keyAlgorithm != "RS256" || kid.isNullOrBlank()) {
                        continue
                    }
                    val modulus: String? = jwk.get("n")?.asText()
                    val exponent: String? = jwk.get("e")?.asText()
                    if (!modulus.isNullOrBlank() && !exponent.isNullOrBlank()) {
                        rsaKeyMap[kid] = toRsaPublicKey(modulus, exponent)
                    }
                }
                this.keyCache.clear()
                this.keyCache.putAll(rsaKeyMap)
                log.debug("JWKS refreshed: {} keys loaded", rsaKeyMap.size)
                return
            } catch (ex: Exception) {
                lastEx = ex as? Exception ?: RuntimeException(ex)
                if (attempt < maxAttempts) {
                    log.debug("JWKS fetch attempt {}/{} failed; retrying...", attempt, maxAttempts)
                }
            }
        }
        throw IllegalStateException("Unable to refresh JWKS from ${this.jwksUri} after $maxAttempts attempts", lastEx)
    }

    private fun toRsaPublicKey(nB64Url: String, eB64Url: String): RSAPublicKey {
        val n = BigInteger(1, decodeBase64Url(nB64Url))
        val e = BigInteger(1, decodeBase64Url(eB64Url))
        val spec = RSAPublicKeySpec(n, e)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(spec) as RSAPublicKey
    }

    private fun decodeBase64Url(value: String): ByteArray {
        var reformattedCode: String = value.replace('-', '+').replace('_', '/')
        val pad: Int = (4 - reformattedCode.length % 4) % 4
        reformattedCode += "=".repeat(pad)
        return Base64.getDecoder().decode(reformattedCode)
    }
}