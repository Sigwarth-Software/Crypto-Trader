package org.cryptotrader.security.library.service

import org.cryptotrader.security.library.service.model.TinkKeysetStore
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.security.GeneralSecurityException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

@DisplayName("Encryption Service")
class EncryptionServiceTest : CryptoTraderTest() {
    private lateinit var encryptionService: EncryptionService
    private lateinit var keysetStore: InMemoryTinkKeysetStore

    @BeforeEach
    fun setUp() {
        this.keysetStore = InMemoryTinkKeysetStore()
        this.encryptionService = EncryptionService(keysetStore = this.keysetStore)
    }

    @Nested
    @DisplayName("Encrypt")
    inner class Encrypt {
        @Test
        @DisplayName("Should obfuscate a given input")
        fun encrypt_ObfuscatesInput() {
            val testText = "Your secret data"
            val testTextBytes: ByteArray = testText.toByteArray(Charsets.UTF_8)
            val encryptedBytes: ByteArray = encryptionService.encrypt(testTextBytes)
            assertNotEquals(testTextBytes, encryptedBytes)
        }
    }

    @Nested
    @DisplayName("Decrypt")
    inner class Decrypt {
        @Test
        @DisplayName("Should decrypt a given input")
        fun decrypt_DecryptsInput() {
            val testText = "Your secret data"
            val testTextBytes: ByteArray = testText.toByteArray(Charsets.UTF_8)
            val encryptedBytes: ByteArray = encryptionService.encrypt(testTextBytes)
            val decryptedBytes: ByteArray = encryptionService.decrypt(encryptedBytes)
            val decryptedText: String = decryptedBytes.toString(Charsets.UTF_8)
            assertEquals(testText, decryptedText)
        }

        @Test
        @DisplayName("Should decrypt data encrypted by another service instance with the same database keyset")
        fun decrypt_DecryptsAcrossServiceInstances() {
            val otherService = EncryptionService(keysetStore = keysetStore)
            val testText = "Your cross-module secret data"
            val encryptedBytes: ByteArray = encryptionService.encrypt(testText.toByteArray(Charsets.UTF_8))
            val decryptedBytes: ByteArray = otherService.decrypt(encryptedBytes)
            val decryptedText: String = decryptedBytes.toString(Charsets.UTF_8)
            assertEquals(testText, decryptedText)
        }

        @Test
        @DisplayName("Should isolate different keyset names")
        fun decrypt_UsesConfiguredDatabaseKeysetName() {
            val firstService = EncryptionService(
                keysetStore = keysetStore,
                keysetName = "default-aead"
            )
            val secondService = EncryptionService(
                keysetStore = keysetStore,
                keysetName = "alternate-aead"
            )
            val testText = "Your ORM-backed secret data"
            val encryptedBytes: ByteArray = firstService.encrypt(testText.toByteArray(Charsets.UTF_8))
            assertFailsWith<GeneralSecurityException> {
                secondService.decrypt(encryptedBytes)
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
