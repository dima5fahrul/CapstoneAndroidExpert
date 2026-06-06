package com.example.capstoneandroidexpert.core.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object SecureKeyManager {

    private const val KEY_ALIAS = "capstone_db_key"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_SIZE = 256

    fun getOrCreateDatabaseKey(): ByteArray {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE)
                    .build()
            )
            keyGenerator.generateKey()
        }

        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        return secretKey.encoded
    }
}
