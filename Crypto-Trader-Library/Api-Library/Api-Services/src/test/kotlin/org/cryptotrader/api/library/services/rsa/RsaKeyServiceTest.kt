package org.cryptotrader.api.library.services.rsa

import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("RsaKeyService")
@UnitSuite
class RsaKeyServiceTest : CryptoTraderTest() {

    private lateinit var rsaKeyService: RsaKeyService

    @Nested
    @DisplayName("constructor")
    inner class Initialization {
        @Nested
        @DisplayName("when keys are configured")
        inner class WhenProvided {
            private var publicPem: String? = null
            private var privatePem: String? = null
            private val configuredKid = "configured-kid"

            @BeforeEach
            fun setUp() {
                val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
                val keyPair: KeyPair = generator.generateKeyPair()
                val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
                val publicEncoded: ByteArray? = keyFactory.getKeySpec(keyPair.public, X509EncodedKeySpec::class.java).encoded
                val privateEncoded: ByteArray? = keyFactory.getKeySpec(keyPair.private, PKCS8EncodedKeySpec::class.java).encoded
                publicPem = Base64.getEncoder().encodeToString(publicEncoded)
                privatePem = Base64.getEncoder().encodeToString(privateEncoded)
            }

            @Test
            @DisplayName("should load keys from configuration")
            fun loadKeysFromConfiguration() {
                rsaKeyService = RsaKeyService(publicPem, privatePem, configuredKid)

                assertNotNull(rsaKeyService.publicKey, "publicKey should be loaded from config")
                assertNotNull(rsaKeyService.privateKey, "privateKey should be loaded from config")
                assertEquals("configured-kid", rsaKeyService.kid, "kid should use configuredKid when provided")
            }
        }

        @Nested
        @DisplayName("when config is absent")
        inner class WhenConfigIsAbsent {
            private val publicPem: String? = null
            private val privatePem: String? = null
            private val configuredKid: String? = null

            @Test
            @DisplayName("should generate ephemeral keys")
            fun generateEphemeralKeys() {
                rsaKeyService = RsaKeyService(publicPem, privatePem, configuredKid)

                assertNotNull(rsaKeyService.publicKey, "publicKey should be generated")
                assertNotNull(rsaKeyService.privateKey, "privateKey should be generated")
                assertNotNull(rsaKeyService.kid, "kid should be computed from public key when not configured")
            }
        }

        @Nested
        @DisplayName("when keys are configured for identifier selection")
        inner class WhenKeysAreConfiguredForIdentifierSelection {
            private var publicPem: String? = null
            private var privatePem: String? = null

            @BeforeEach
            fun setUp() {
                val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
                val keyPair: KeyPair = generator.generateKeyPair()
                val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
                val publicEncoded: ByteArray? = keyFactory.getKeySpec(keyPair.public, X509EncodedKeySpec::class.java).encoded
                val privateEncoded: ByteArray? = keyFactory.getKeySpec(keyPair.private, PKCS8EncodedKeySpec::class.java).encoded
                publicPem = Base64.getEncoder().encodeToString(publicEncoded)
                privatePem = Base64.getEncoder().encodeToString(privateEncoded)
            }

            @Nested
            @DisplayName("when the key identifier is configured")
            inner class WhenKeyIdentifierIsConfigured {
                @Test
                @DisplayName("should use the configured identifier")
                fun useConfiguredIdentifier() {
                    rsaKeyService = RsaKeyService(publicPem, privatePem, "fixed-kid")
                    assertEquals("fixed-kid", rsaKeyService.kid, "kid should equal configuredKid when provided")
                }
            }

            @Nested
            @DisplayName("when the key identifier is absent")
            inner class WhenKeyIdentifierIsAbsent {
                @Test
                @DisplayName("should compute the identifier")
                fun computeIdentifier() {
                    rsaKeyService = RsaKeyService(publicPem, privatePem, null)
                    assertNotNull(rsaKeyService.kid, "kid should be computed when not configured")
                }
            }
        }
    }
}
