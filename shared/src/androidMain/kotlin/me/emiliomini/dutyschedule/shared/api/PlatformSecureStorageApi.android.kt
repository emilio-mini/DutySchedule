package me.emiliomini.dutyschedule.shared.api

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts with an AES-GCM key that never leaves the Android Keystore; only the IV and the
 * ciphertext are written to shared preferences
 */
class AndroidSecureStorageApi : PlatformSecureStorageApi {
    private val logger = getPlatformLogger("AndroidSecureStorageApi")

    private val preferences by lazy {
        APPLICATION_CONTEXT.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        const val PREFERENCES_NAME = "secure_credentials"
        const val KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "dutyschedule_credentials"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
        const val IV_LENGTH = 12
    }

    override suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        val stored = preferences.getString(key, null) ?: return@withContext null

        try {
            val blob = Base64.decode(stored, Base64.NO_WRAP)
            if (blob.size <= IV_LENGTH) {
                logger.warn("Stored secret for '$key' is truncated - dropping it")
                remove(key)
                return@withContext null
            }

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(GCM_TAG_BITS, blob, 0, IV_LENGTH)
            )
            String(
                cipher.doFinal(blob, IV_LENGTH, blob.size - IV_LENGTH),
                Charsets.UTF_8
            )
        } catch (e: GeneralSecurityException) {
            logger.warn("Could not decrypt secret for '$key' - dropping it", throwable = e)
            remove(key)
            null
        } catch (e: IllegalArgumentException) {
            logger.warn("Stored secret for '$key' is not valid Base64 - dropping it", throwable = e)
            remove(key)
            null
        }
    }

    override suspend fun set(key: String, value: String) = withContext(Dispatchers.IO) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey())

            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val blob = cipher.iv + encrypted

            preferences.edit()
                .putString(key, Base64.encodeToString(blob, Base64.NO_WRAP))
                .commit()
            Unit
        } catch (e: GeneralSecurityException) {
            logger.error("Could not encrypt secret for '$key'", throwable = e)
            Unit
        }
    }

    override suspend fun remove(key: String) {
        preferences.edit().remove(key).commit()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) {
            return existing.secretKey
        }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }
}

actual fun initializePlatformSecureStorageApi(): PlatformSecureStorageApi {
    return AndroidSecureStorageApi()
}
