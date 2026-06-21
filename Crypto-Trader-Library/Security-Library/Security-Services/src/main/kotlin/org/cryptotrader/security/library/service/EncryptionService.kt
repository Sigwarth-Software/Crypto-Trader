package org.cryptotrader.security.library.service

import com.google.crypto.tink.Aead
import com.google.crypto.tink.InsecureSecretKeyAccess
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.TinkJsonProtoKeysetFormat
import com.google.crypto.tink.config.TinkConfig
import org.cryptotrader.security.library.service.model.TinkKeysetStore
import org.slf4j.LoggerFactory

class EncryptionService(
    private val keysetStore: TinkKeysetStore,
    private val keysetName: String = "default-aead",
    private val generateIfMissing: Boolean = true
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val aead: Aead = this.createAeadFromKeyset()

    fun encrypt(bytes: ByteArray): ByteArray {
        return this.aead.encrypt(bytes, null)
    }

    fun encrypt(bytes: ByteArray, associatedData: ByteArray?): ByteArray {
        return this.aead.encrypt(bytes, associatedData)
    }

    fun decrypt(bytes: ByteArray): ByteArray = this.aead.decrypt(bytes, null)

    fun decrypt(bytes: ByteArray, associatedData: ByteArray?): ByteArray {
        return this.aead.decrypt(bytes, associatedData)
    }

    private fun createAeadFromKeyset(): Aead {
        TinkConfig.register()
        this.log.info("Initializing encryption (Google Tink AEAD). Keyset source configured as database row: {}", this.keysetName)
        return this.loadKeysetFromStore()
    }

    private fun loadKeysetFromStore(): Aead {
        val keysetJson = this.keysetStore.load(this.keysetName)
            ?: this.generateAndSaveKeyset()
        val handle: KeysetHandle = TinkJsonProtoKeysetFormat.parseKeyset(keysetJson, InsecureSecretKeyAccess.get())
        return handle.getPrimitive(Aead::class.java)
    }

    private fun generateAndSaveKeyset(): String {
        if (!this.generateIfMissing) {
            throw IllegalStateException("Tink keyset not found in database store: ${this.keysetName}")
        }
        val handle: KeysetHandle = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
        val keysetJson: String = TinkJsonProtoKeysetFormat.serializeKeyset(handle, InsecureSecretKeyAccess.get())
        return this.keysetStore.saveIfAbsent(this.keysetName, keysetJson)
    }
}
