package org.cryptotrader.api.library.services.dpop

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.time.Instant
import java.util.*
import java.util.Base64
import org.cryptotrader.api.library.model.dpop.DpopVerificationResult
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy

@DisplayName("DpopVerifierService")
@UnitSuite
class DpopVerifierServiceTest : CryptoTraderTest() {

    private lateinit var dpopVerifierService: DpopVerifierService

    private val allowedAlgorithms = "ES256"

    @BeforeEach
    fun setUp() {
        this.dpopVerifierService = spy(DpopVerifierService(this.allowedAlgorithms))

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
    @DisplayName("verify")
    inner class Verify {
        @Nested
        @DisplayName("when the proof is valid")
        inner class WhenTheProofIsValid {
            private lateinit var requestMethod: String
            private lateinit var requestUri: String
            private lateinit var dpop: String

            @BeforeEach
            fun setUp() {
                requestMethod = "GET"
                requestUri = "http://localhost:8080/api/auth/login"
                dpop = createValidDpopProof(requestMethod, requestUri)
            }

            @Test
            @DisplayName("should verify valid DPoP proof and return result")
            fun verifyValidDPoPProofAndReturnResult() {
                val result: DpopVerificationResult? = dpopVerifierService.verify(dpop, requestMethod, requestUri)
                assertNotNull(result)
            }
        }

        @Nested
        @DisplayName("when the proof is empty")
        inner class WhenTheProofIsEmpty {
            @Test
            @DisplayName("should return null on invalid DPoP proof")
            fun returnNullOnInvalidDPoPProof() {
                val result: DpopVerificationResult? = dpopVerifierService.verify("", "GET", "http://localhost:8080/api/auth/login")
                assertNull(result)
            }
        }

        @Nested
        @DisplayName("when disallowed algorithm configured")
        inner class WhenDisallowedAlgorithmConfigured {
            private lateinit var requestMethod: String
            private lateinit var uri: String
            private lateinit var dpop: String
            private lateinit var service: DpopVerifierService

            @BeforeEach
            fun setUp() {
                requestMethod = "GET"
                uri = "http://localhost:8080/api/auth/login"
                dpop = createValidDpopProof(requestMethod, uri) // ES256
                service = DpopVerifierService("RS256")
            }

            @Test
            @DisplayName("should return null")
            fun returnNull() {
                val result = service.verify(dpop, requestMethod, uri)
                assertNull(result)
            }
        }

        @Nested
        @DisplayName("when allowed algorithm configured")
        inner class WhenAllowedAlgorithmConfigured {
            private lateinit var requestMethod: String
            private lateinit var uri: String
            private lateinit var dpop: String
            private lateinit var service: DpopVerifierService

            @BeforeEach
            fun setUp() {
                requestMethod = "GET"
                uri = "http://localhost:8080/api/auth/login"
                dpop = createValidDpopProof(requestMethod, uri) // ES256
                service = DpopVerifierService("ES256")
            }

            @Test
            @DisplayName("should return non-null")
            fun returnNonNull() {
                val result = service.verify(dpop, requestMethod, uri)
                assertNotNull(result)
            }
        }

        @Nested
        @DisplayName("when the URI differs only by its default port")
        inner class WhenTheURIDiffersOnlyByItsDefaultPort {
            private lateinit var method: String
            private lateinit var uriWithDefaultPort: String
            private lateinit var dpop: String

            @BeforeEach
            fun setUp() {
                method = "GET"
                val uriWithoutPort = "http://localhost/api/auth/login"
                uriWithDefaultPort = "http://localhost:80/api/auth/login"
                dpop = createValidDpopProof(method, uriWithoutPort)
            }

            @Test
            @DisplayName("should normalize equivalent URIs")
            fun normalizeEquivalentURIs() {
                val result = dpopVerifierService.verify(dpop, method, uriWithDefaultPort)
                assertNotNull(result)
            }
        }

        @Nested
        @DisplayName("when the URI ports differ")
        inner class WhenTheURIPortsDiffer {
            private lateinit var method: String
            private lateinit var uriB: String
            private lateinit var dpop: String

            @BeforeEach
            fun setUp() {
                method = "GET"
                val uriA = "http://localhost:81/api/auth/login"
                uriB = "http://localhost:80/api/auth/login"
                dpop = createValidDpopProof(method, uriA)
            }

            @Test
            @DisplayName("should detect different ports between URI and scheme")
            fun detectDifferentPortsBetweenURIAndScheme() {
                val result = dpopVerifierService.verify(dpop, method, uriB)
                assertNull(result)
            }
        }

        @Nested
        @DisplayName("when the proof algorithm is configured")
        inner class WhenTheProofAlgorithmIsConfigured {
            private lateinit var method: String
            private lateinit var uri: String
            private lateinit var dpop: String
            private lateinit var service: DpopVerifierService

            @BeforeEach
            fun setUp() {
                method = "GET"
                uri = "http://localhost:8080/api/auth/login"
                dpop = createValidDpopProof(method, uri) // ES256
                service = DpopVerifierService("ES256,RS256")
            }

            @Test
            @DisplayName("should allow configured algorithms")
            fun allowConfiguredAlgorithms() {
                val result = service.verify(dpop, method, uri)
                assertNotNull(result)
            }
        }

        @Nested
        @DisplayName("when the proof algorithm is not configured")
        inner class WhenTheProofAlgorithmIsNotConfigured {
            private lateinit var method: String
            private lateinit var uri: String
            private lateinit var dpop: String
            private lateinit var service: DpopVerifierService

            @BeforeEach
            fun setUp() {
                method = "GET"
                uri = "http://localhost:8080/api/auth/login"
                dpop = createValidDpopProof(method, uri) // ES256
                service = DpopVerifierService("RS256")
            }

            @Test
            @DisplayName("should reject disallowed algorithms")
            fun rejectDisallowedAlgorithms() {
                val result = service.verify(dpop, method, uri)
                assertNull(result)
            }
        }

    }

    @Nested
    @DisplayName("isValidProof")
    inner class IsValidProof {
        @Nested
        @DisplayName("when the proof has segments")
        inner class WhenProofHasSegments {
            @Test
            @DisplayName("should return true")
            fun returnTrue() {
                assert(dpopVerifierService.isValidProof("abc.def.ghi"))
            }
        }

        @Nested
        @DisplayName("when the proof is nonempty")
        inner class WhenProofIsNonempty {
            @Test
            @DisplayName("should return true")
            fun returnTrue() {
                assert(dpopVerifierService.isValidProof("x"))
            }
        }

        @Nested
        @DisplayName("when the proof is null")
        inner class WhenProofIsNull {
            @Test
            @DisplayName("should return false")
            fun returnFalse() {
                assert(!dpopVerifierService.isValidProof(null))
            }
        }

        @Nested
        @DisplayName("when the proof is empty")
        inner class WhenProofIsEmpty {
            @Test
            @DisplayName("should return false")
            fun returnFalse() {
                assert(!dpopVerifierService.isValidProof(""))
            }
        }

        @Nested
        @DisplayName("when the proof contains only whitespace")
        inner class WhenProofIsWhitespace {
            @Test
            @DisplayName("should return false")
            fun returnFalse() {
                assert(!dpopVerifierService.isValidProof("   "))
            }
        }
    }

}
