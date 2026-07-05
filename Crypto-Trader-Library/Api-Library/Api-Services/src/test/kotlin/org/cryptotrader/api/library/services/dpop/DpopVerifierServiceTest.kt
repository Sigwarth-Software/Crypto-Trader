package org.cryptotrader.api.library.services.dpop

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.cryptotrader.api.library.services.rsa.RsaKeyService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.interfaces.ECPrivateKey
import java.time.Instant
import java.util.*
import java.security.spec.ECGenParameterSpec
import java.util.Base64

@Tag("DpopVerifierService")
@Tag("service")
@DisplayName("DPoP Verifier Service")
class DpopVerifierServiceTest : CryptoTraderTest() {

    private lateinit var dpopVerifierService: DpopVerifierService
    private lateinit var token: String

    private val allowedAlgorithms = "ES256"

    @BeforeEach
    fun setUp() {
        this.dpopVerifierService = spy(DpopVerifierService(this.allowedAlgorithms))
        val now = Instant.now()
        val rsaService: RsaKeyService = RsaKeyService(null, null, null)
        val algorithm = Algorithm.RSA256(rsaService.publicKey, rsaService.privateKey)
        this.token = JWT.create()
            .withIssuer("https://sscryptotrader.com")
            .withSubject("user")
            .withAudience("audience")
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(300)))
            .withJWTId(UUID.randomUUID().toString())
            .withClaim("email", "user@example.com")
            .sign(algorithm)
    }

    private fun base64UrlNoPad(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private fun bigIntToFixed(bytes: ByteArray, size: Int): ByteArray {
        var arr = bytes
        if (arr.size > size) {
            // Strip leading sign byte(s)
            val start = arr.size - size
            arr = arr.copyOfRange(start, arr.size)
        }
        if (arr.size < size) {
            val pad = ByteArray(size - arr.size)
            return pad + arr
        }
        return arr
    }

    private fun createValidDpopProof(method: String, uri: String): String {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        val kp: KeyPair = kpg.generateKeyPair()
        val ecPub = kp.public as ECPublicKey
        val wx = ecPub.w.affineX.toByteArray()
        val wy = ecPub.w.affineY.toByteArray()
        val xB64 = base64UrlNoPad(bigIntToFixed(wx, 32))
        val yB64 = base64UrlNoPad(bigIntToFixed(wy, 32))
        val jwk = mapOf(
            "kty" to "EC",
            "crv" to "P-256",
            "x" to xB64,
            "y" to yB64
        )
        val header = mutableMapOf<String, Any>(
            "typ" to "dpop+jwt",
            "jwk" to jwk
        )
        // The alg is set by the signer; no need to include in header map explicitly.
        val nowSec = Instant.now().epochSecond
        return JWT.create()
            .withHeader(header)
            .withClaim("htm", method)
            .withClaim("htu", uri)
            .withClaim("iat", nowSec)
            .withJWTId(UUID.randomUUID().toString())
            .sign(Algorithm.ECDSA256(ecPub, kp.private as ECPrivateKey))
    }

    @Nested
    @Tag("verify")
    @DisplayName("Verify")
    inner class Verify {
        @Test
        @DisplayName("Should verify valid DPoP proof and return result")
        fun verify_ReturnsResult_OnValidProof() {
            val requestMethod = "GET"
            val requestUri = "http://localhost:8080/api/auth/login"

            val dpop = createValidDpopProof(requestMethod, requestUri)
            val result: DpopVerifierService.VerificationResult? = dpopVerifierService.verify(dpop, requestMethod, requestUri)
            assertNotNull(result)
        }

        @Test
        @DisplayName("Should return null on invalid DPoP proof")
        fun verify_ReturnsNull_OnInvalidProof() {
            val result: DpopVerifierService.VerificationResult? = dpopVerifierService.verify("", "GET", "http://localhost:8080/api/auth/login")
            assertNull(result)
        }
    }

    @Nested
    @Tag("isTokenMatch")
    @DisplayName("Token Match")
    inner class IsTokenMatch {
        @Test
        @DisplayName("Should return null when disallowed algorithm configured")
        fun verify_ReturnsNull_WhenAlgorithmDisallowed() {
            val requestMethod = "GET"
            val uri = "http://localhost:8080/api/auth/login"
            val dpop = createValidDpopProof(requestMethod, uri) // ES256
            // Recreate service that only allows RS256; ES256 should be rejected by isAllowedAlgorithm
            val service = DpopVerifierService("RS256")
            val result = service.verify(dpop, requestMethod, uri)
            assertNull(result)
        }

        @Test
        @DisplayName("Should return non-null when allowed algorithm configured")
        fun verify_ReturnsResult_WhenAlgorithmAllowed() {
            val requestMethod = "GET"
            val uri = "http://localhost:8080/api/auth/login"
            val dpop = createValidDpopProof(requestMethod, uri) // ES256
            val service = DpopVerifierService("ES256")
            val result = service.verify(dpop, requestMethod, uri)
            assertNotNull(result)
        }
    }

    @Nested
    @Tag("isJwtType")
    @DisplayName("JWT Type")
    inner class IsJwtType {
        @Test
        @DisplayName("Should return true for DPoP type")
        fun isJwtType_ReturnsTrue_ForDpop() { }

        @Test
        @DisplayName("Should return false for non-DPoP type")
        fun isJwtType_ReturnsFalse_ForNonDpop() { }
    }

    @Nested
    @Tag("generateVerifierByAlgorithm")
    @DisplayName("Generate Verifier By Algorithm")
    inner class GenerateVerifierByAlgorithm {
        @Test
        @DisplayName("Should return verifier for supported algorithms")
        fun generateVerifierByAlgorithm_ReturnsVerifier_ForSupportedAlg() { }

        @Test
        @DisplayName("Should return null for unsupported algorithms")
        fun generateVerifierByAlgorithm_ReturnsNull_ForUnsupportedAlg() { }
    }

    @Nested
    @Tag("buildPublicKeyFromJwk")
    @DisplayName("Build Public Key From JWK")
    inner class BuildPublicKeyFromJwk {
        @Test
        @DisplayName("Should build RSA public key from JWK")
        fun buildPublicKeyFromJwk_BuildsRsa() { }

        @Test
        @DisplayName("Should build EC public key from JWK")
        fun buildPublicKeyFromJwk_BuildsEc() { }

        @Test
        @DisplayName("Should return null for invalid JWK")
        fun buildPublicKeyFromJwk_ReturnsNull_ForInvalid() { }
    }

    @Nested
    @Tag("buildRsaPublicKey")
    @DisplayName("Build RSA Public Key")
    inner class BuildRsaPublicKey {
        @Test
        @DisplayName("Should build RSA key from modulus and exponent")
        fun buildRsaPublicKey_BuildsKey() { }
    }

    @Nested
    @Tag("buildEcPublicKey")
    @DisplayName("Build EC Public Key")
    inner class BuildEcPublicKey {
        @Test
        @DisplayName("Should build EC key from curve and coordinates")
        fun buildEcPublicKey_BuildsKey() { }
    }

    @Nested
    @Tag("ecSpecForCurve")
    @DisplayName("EC Spec For Curve")
    inner class EcSpecForCurve {
        @Test
        @DisplayName("Should return spec for supported curves")
        fun ecSpecForCurve_ReturnsSpec_ForSupported() { }

        @Test
        @DisplayName("Should return null for unsupported curves")
        fun ecSpecForCurve_ReturnsNull_ForUnsupported() { }
    }

    @Nested
    @Tag("normalizeUri")
    @DisplayName("Normalize URI")
    inner class NormalizeUri {
        @Test
        @DisplayName("Should normalize equivalent URIs")
        fun normalizeUri_Normalizes() {
            val method = "GET"
            val uriWithoutPort = "http://localhost/api/auth/login"
            val uriWithDefaultPort = "http://localhost:80/api/auth/login"
            // Build DPoP for one URI and verify against the other equivalent form
            val dpop = createValidDpopProof(method, uriWithoutPort)
            val result = dpopVerifierService.verify(dpop, method, uriWithDefaultPort)
            assertNotNull(result)
        }
    }

    @Nested
    @Tag("hasDifferentPort")
    @DisplayName("Has Different Port")
    inner class HasDifferentPort {
        @Test
        @DisplayName("Should detect different ports between URI and scheme")
        fun hasDifferentPort_DetectsDifferences() {
            val method = "GET"
            val uriA = "http://localhost:81/api/auth/login"
            val uriB = "http://localhost:80/api/auth/login"
            val dpop = createValidDpopProof(method, uriA)
            val result = dpopVerifierService.verify(dpop, method, uriB)
            assertNull(result)
        }
    }

    @Nested
    @Tag("defaultPort")
    @DisplayName("Default Port")
    inner class DefaultPort {
        @Test
        @DisplayName("Should return default port for known schemes")
        fun defaultPort_ReturnsPort() { }
    }

    @Nested
    @Tag("sha256")
    @DisplayName("SHA-256")
    inner class Sha256 {
        @Test
        @DisplayName("Should compute SHA-256 for given input")
        fun sha256_Computes() { }
    }

    @Nested
    @Tag("base64UrlEncode")
    @DisplayName("Base64Url Encode")
    inner class Base64UrlEncode {
        @Test
        @DisplayName("Should encode bytes to base64url")
        fun base64UrlEncode_Encodes() { }
    }

    @Nested
    @Tag("base64UrlDecode")
    @DisplayName("Base64Url Decode")
    inner class Base64UrlDecode {
        @Test
        @DisplayName("Should decode base64url to bytes")
        fun base64UrlDecode_Decodes() { }
    }

    @Nested
    @Tag("computeExpectedAth")
    @DisplayName("Compute Expected ATH")
    inner class ComputeExpectedAth {
        @Test
        @DisplayName("Should compute expected 'ath' claim from access token")
        fun computeExpectedAth_Computes() { }
    }

    @Nested
    @Tag("computeJwkThumbprint")
    @DisplayName("Compute JWK Thumbprint")
    inner class ComputeJwkThumbprint {
        @Test
        @DisplayName("Should compute correct JWK thumbprint")
        fun computeJwkThumbprint_Computes() { }

        @Test
        @DisplayName("Should return null when required fields missing")
        fun computeJwkThumbprint_ReturnsNull_WhenMissingFields() { }
    }

    @Nested
    @Tag("canonicalizeJson")
    @DisplayName("Canonicalize JSON")
    inner class CanonicalizeJson {
        @Test
        @DisplayName("Should canonicalize JSON map deterministically")
        fun canonicalizeJson_Canonicalizes() { }
    }

    @Nested
    @Tag("appendJsonKeyValue")
    @DisplayName("Append JSON Key/Value")
    inner class AppendJsonKeyValue {
        @Test
        @DisplayName("Should append escaped key/value pairs to StringBuilder")
        fun appendJsonKeyValue_Appends() { }
    }

    @Nested
    @Tag("escapeJson")
    @DisplayName("Escape JSON")
    inner class EscapeJson {
        @Test
        @DisplayName("Should escape JSON special characters")
        fun escapeJson_Escapes() { }
    }

    @Nested
    @Tag("isValidProof")
    @DisplayName("Is Valid Proof")
    inner class IsValidProof {
        @Test
        @DisplayName("Should detect non-empty valid proof")
        fun isValidProof_Detects() {
            // true cases
            assert(dpopVerifierService.isValidProof("abc.def.ghi"))
            assert(dpopVerifierService.isValidProof("x"))
            // false cases
            assert(!dpopVerifierService.isValidProof(null))
            assert(!dpopVerifierService.isValidProof(""))
            assert(!dpopVerifierService.isValidProof("   "))
        }
    }

    @Nested
    @Tag("isAllowedAlgorithm")
    @DisplayName("Is Allowed Algorithm")
    inner class IsAllowedAlgorithm {
        @Test
        @DisplayName("Should allow configured algorithms")
        fun isAllowedAlgorithm_AllowsConfigured() {
            val method = "GET"
            val uri = "http://localhost:8080/api/auth/login"
            val dpop = createValidDpopProof(method, uri) // ES256
            // This instance allows ES256
            val service = DpopVerifierService("ES256,RS256")
            val result = service.verify(dpop, method, uri)
            assertNotNull(result)
        }

        @Test
        @DisplayName("Should reject disallowed algorithms")
        fun isAllowedAlgorithm_RejectsDisallowed() {
            val method = "GET"
            val uri = "http://localhost:8080/api/auth/login"
            val dpop = createValidDpopProof(method, uri) // ES256
            // This instance does not allow ES256
            val service = DpopVerifierService("RS256")
            val result = service.verify(dpop, method, uri)
            assertNull(result)
        }
    }
}