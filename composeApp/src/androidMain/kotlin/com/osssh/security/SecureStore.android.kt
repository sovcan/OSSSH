package com.osssh.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AndroidSecureStore(context: Context) : SecureStore {
    private val masterKey = MasterKey.Builder(context)
        .setKeyGenParameterSpec(
            KeyGenParameterSpec.Builder(
                "osssh_master",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()
        ).build()

    private val prefs = EncryptedSharedPreferences.create(
        context, "osssh_secrets", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override suspend fun put(key: String, secret: String) =
        Unit.apply { prefs.edit().putString(key, secret).apply() }

    override suspend fun get(key: String): String? = prefs.getString(key, null)

    override suspend fun remove(key: String) =
        Unit.apply { prefs.edit().remove(key).apply() }
}
