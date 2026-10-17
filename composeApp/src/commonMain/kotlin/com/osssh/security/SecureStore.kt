package com.osssh.security

/** Platform-abstracted secret storage. Android: Keystore-backed EncryptedSharedPreferences.
 *  Desktop: AES-GCM encrypted file with a locally generated key (0600 perms). */
interface SecureStore {
    suspend fun put(key: String, secret: String)
    suspend fun get(key: String): String?
    suspend fun remove(key: String)
}
