package com.osssh.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.security.SecureRandom
import java.util.Base64
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** AES-256-GCM store. Key lives in ~/.osssh/key.bin with 0600 permissions (POSIX). */
class DesktopSecureStore(
    private val dir: File = File(System.getProperty("user.home"), ".osssh")
) : SecureStore {
    private val secretsFile get() = File(dir, "secrets.enc.properties")
    private val key by lazy {
        val kf = File(dir, "key.bin")
        if (dir.mkdirs()) Files.setPosixFilePermissions(dir.toPath(), PosixFilePermissions.fromString("rwx------"))
        if (kf.exists()) {
            SecretKeySpec(kf.readBytes(), "AES")
        } else {
            val k = KeyGenerator.getInstance("AES").apply { init(256, SecureRandom()) }.generateKey()
            kf.writeBytes(k.encoded)
            Files.setPosixFilePermissions(kf.toPath(), PosixFilePermissions.fromString("rw-------"))
            SecretKeySpec(k.encoded, "AES")
        }
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val iv = ByteArray(12).also(SecureRandom()::nextBytes)
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        return iv + c.doFinal(plain)
    }

    private fun decrypt(blob: ByteArray): ByteArray {
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, blob.copyOfRange(0, 12)))
        return c.doFinal(blob.copyOfRange(12, blob.size))
    }

    private suspend fun props(): Properties = withContext(Dispatchers.IO) {
        Properties().also { p -> secretsFile.takeIf { it.exists() }?.inputStream()?.use { p.load(it) } }
    }

    private suspend fun save(p: Properties) = withContext(Dispatchers.IO) {
        secretsFile.outputStream().use { p.store(it, null) }
    }

    override suspend fun put(key: String, secret: String) {
        save(props().apply {
            setProperty(key, Base64.getEncoder().encodeToString(encrypt(secret.toByteArray())))
        })
    }

    override suspend fun get(key: String): String? = props().getProperty(key)
        ?.let { String(decrypt(Base64.getDecoder().decode(it))) }

    override suspend fun remove(key: String) {
        save(props().apply { remove(key) })
    }
}
