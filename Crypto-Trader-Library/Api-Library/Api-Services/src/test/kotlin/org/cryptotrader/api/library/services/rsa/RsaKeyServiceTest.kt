package org.cryptotrader.api.library.services.rsa

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Tag("RsaKeyService")
@Tag("service")
@DisplayName("RSA Key Service")
class RsaKeyServiceTest : CryptoTraderTest() {

    private val publicPem: String? = null
    private val privatePem: String? = null
    private val configuredKid: String? = null

    @InjectMocks
    private lateinit var rsaKeyService: RsaKeyService

    @BeforeEach
    fun setUp() {
        this.rsaKeyService = RsaKeyService(publicPem, privatePem, configuredKid)
    }

    @Nested
    @Tag("initialization")
    @DisplayName("Initialization")
    inner class Initialization {
        @Test
        @DisplayName("Should load keys from configuration when provided")
        fun initialization_LoadsFromConfig_WhenProvided() {
            val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
            val keyPair: KeyPair = generator.generateKeyPair()
            val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
            val publicEncoded: ByteArray? = keyFactory.getKeySpec(keyPair.public, X509EncodedKeySpec::class.java).encoded
            val privateEncoded: ByteArray? = keyFactory.getKeySpec(keyPair.private, PKCS8EncodedKeySpec::class.java).encoded
            val publicPem: String? = Base64.getEncoder().encodeToString(publicEncoded)
            val privatePem: String? = Base64.getEncoder().encodeToString(privateEncoded)
            val configuredKid = "configured-kid"

            rsaKeyService = RsaKeyService(publicPem, privatePem, configuredKid)

            assertNotNull(rsaKeyService.publicKey, "publicKey should be loaded from config")
            assertNotNull(rsaKeyService.privateKey, "privateKey should be loaded from config")
            assertEquals("configured-kid", rsaKeyService.kid, "kid should use configuredKid when provided")
        }

        @Test
        @DisplayName("Should generate ephemeral keys when config is absent")
        fun initialization_GeneratesEphemeral_WhenAbsent() {
            val publicPem: String? = null
            val privatePem: String? = null
            val configuredKid: String? = null

            rsaKeyService = RsaKeyService(publicPem, privatePem, configuredKid)

            assertNotNull(rsaKeyService.publicKey, "publicKey should be generated")
            assertNotNull(rsaKeyService.privateKey, "privateKey should be generated")
            assertNotNull(rsaKeyService.kid, "kid should be computed from public key when not configured")
        }

        @Test
        @DisplayName("Should compute or use configured kid")
        fun initialization_ComputesOrUsesKid() {
            val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
            val keyPair: KeyPair = generator.generateKeyPair()
            val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
            val publicEncoded: ByteArray? = keyFactory.getKeySpec(keyPair.public, X509EncodedKeySpec::class.java).encoded
            val privateEncoded: ByteArray? = keyFactory.getKeySpec(keyPair.private, PKCS8EncodedKeySpec::class.java).encoded
            val publicPem: String? = Base64.getEncoder().encodeToString(publicEncoded)
            val privatePem: String? = Base64.getEncoder().encodeToString(privateEncoded)

            rsaKeyService = RsaKeyService(publicPem, privatePem, "fixed-kid")
            assertEquals("fixed-kid", rsaKeyService.kid, "kid should equal configuredKid when provided")

            rsaKeyService = RsaKeyService(publicPem, privatePem, null)
            assertNotNull(rsaKeyService.kid, "kid should be computed when not configured")
        }
    }
}