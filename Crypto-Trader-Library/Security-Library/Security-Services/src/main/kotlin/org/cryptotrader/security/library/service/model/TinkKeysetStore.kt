package org.cryptotrader.security.library.service.model

interface TinkKeysetStore {
    fun load(id: String): String?
    fun saveIfAbsent(id: String, keysetJson: String): String
}