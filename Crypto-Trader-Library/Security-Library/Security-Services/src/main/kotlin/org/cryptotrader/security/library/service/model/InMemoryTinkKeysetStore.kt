package org.cryptotrader.security.library.service.model

import java.util.concurrent.ConcurrentHashMap

class InMemoryTinkKeysetStore : TinkKeysetStore {
    private val keysets = ConcurrentHashMap<String, String>()

    override fun load(id: String): String? = this.keysets[id]

    override fun saveIfAbsent(id: String, keysetJson: String): String =
        this.keysets.putIfAbsent(id, keysetJson) ?: keysetJson
}
