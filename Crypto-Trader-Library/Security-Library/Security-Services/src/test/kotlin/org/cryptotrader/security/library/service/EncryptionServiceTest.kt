package org.cryptotrader.security.library.service

import java.security.GeneralSecurityException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import org.cryptotrader.security.library.service.model.TinkKeysetStore
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("EncryptionService")
@UnitSuite
class EncryptionServiceTest : CryptoTraderTest() {
    private lateinit var encryptionService: EncryptionService
    private lateinit var keysetStore: InMemoryTinkKeysetStore

    @BeforeEach
    fun setUp() {
        this.keysetStore = InMemoryTinkKeysetStore()
        this.encryptionService = EncryptionService(keysetStore = this.keysetStore)
    }

    @Nested
    @DisplayName("encrypt")
    inner class Encrypt {
        @Test
        @DisplayName("should obfuscate a given input")
        fun obfuscateAGivenInput() {
            val testText = "Your secret data"
            val testTextBytes: ByteArray = testText.toByteArray(Charsets.UTF_8)
            val encryptedBytes: ByteArray = encryptionService.encrypt(testTextBytes)
            assertNotEquals(testTextBytes, encryptedBytes)
        }
    }

    @Nested
    @DisplayName("decrypt")
    inner class Decrypt {
        @Test
        @DisplayName("should decrypt a given input")
        fun decryptAGivenInput() {
            val testText = "Your secret data"
            val testTextBytes: ByteArray = testText.toByteArray(Charsets.UTF_8)
            val encryptedBytes: ByteArray = encryptionService.encrypt(testTextBytes)
            val decryptedBytes: ByteArray = encryptionService.decrypt(encryptedBytes)
            val decryptedText: String = decryptedBytes.toString(Charsets.UTF_8)
            assertEquals(testText, decryptedText)
        }

        @Nested
        @DisplayName("when service instances share a keyset")
        inner class WhenServiceInstancesShareAKeyset {
            private lateinit var otherService: EncryptionService
            private lateinit var testText: String
            private lateinit var encryptedBytes: ByteArray

            @BeforeEach
            fun setUp() {
                otherService = EncryptionService(keysetStore = keysetStore)
                testText = "Your cross-module secret data"
                encryptedBytes = encryptionService.encrypt(testText.toByteArray(Charsets.UTF_8))
            }

            @Test
            @DisplayName("should decrypt data from another service instance")
            fun decryptAcrossServiceInstances() {
                val decryptedBytes: ByteArray = otherService.decrypt(encryptedBytes)
                val decryptedText: String = decryptedBytes.toString(Charsets.UTF_8)
                assertEquals(testText, decryptedText)
            }
        }

        @Nested
        @DisplayName("when service instances use different keyset names")
        inner class WhenServiceInstancesUseDifferentKeysetNames {
            private lateinit var secondService: EncryptionService
            private lateinit var encryptedBytes: ByteArray

            @BeforeEach
            fun setUp() {
                val firstService = EncryptionService(
                    keysetStore = keysetStore,
                    keysetName = "default-aead"
                )
                secondService = EncryptionService(
                    keysetStore = keysetStore,
                    keysetName = "alternate-aead"
                )
                val testText = "Your ORM-backed secret data"
                encryptedBytes = firstService.encrypt(testText.toByteArray(Charsets.UTF_8))
            }

            @Test
            @DisplayName("should isolate different keyset names")
            fun isolateDifferentKeysetNames() {
                assertFailsWith<GeneralSecurityException> {
                    secondService.decrypt(encryptedBytes)
                }
            }
        }
    }

    private class InMemoryTinkKeysetStore : TinkKeysetStore {
        private val keysets: MutableMap<String, String> = mutableMapOf()

        override fun load(id: String): String? = this.keysets[id]

        override fun saveIfAbsent(id: String, keysetJson: String): String {
            return this.keysets.getOrPut(id) { keysetJson }
        }
    }
}
