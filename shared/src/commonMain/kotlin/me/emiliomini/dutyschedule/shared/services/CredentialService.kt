package me.emiliomini.dutyschedule.shared.services

import me.emiliomini.dutyschedule.shared.api.getPlatformLogger
import me.emiliomini.dutyschedule.shared.api.getPlatformSecureStorageApi
import me.emiliomini.dutyschedule.shared.services.storage.StorageService

object CredentialService {
    private const val PASSWORD_KEY = "account_password"

    private val logger = getPlatformLogger("CredentialService")
    private val secureStorage = getPlatformSecureStorageApi()

    suspend fun getPassword(): String? = secureStorage.get(PASSWORD_KEY)

    suspend fun setPassword(password: String) {
        secureStorage.set(PASSWORD_KEY, password)
    }

    suspend fun clearPassword() {
        secureStorage.remove(PASSWORD_KEY)
    }

    /**
     * Moves a password written by an older build out of [StorageService.USER_PREFERENCES], where it
     * was kept in plaintext, and blanks the field
     */
    suspend fun migrateLegacyPlaintextPassword() {
        val legacy = StorageService.USER_PREFERENCES.get()?.password
        if (legacy.isNullOrBlank()) {
            return
        }

        logger.w("Migrating plaintext password out of user preferences")
        setPassword(legacy)
        StorageService.USER_PREFERENCES.update { it.copy(password = "") }
    }
}
