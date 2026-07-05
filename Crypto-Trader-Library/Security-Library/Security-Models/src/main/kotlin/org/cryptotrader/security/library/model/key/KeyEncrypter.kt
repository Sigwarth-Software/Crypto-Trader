package org.cryptotrader.security.library.model.key

interface KeyEncrypter {
    fun encrypt(key: String): String
    fun decrypt(key: String): String
}