@file:OptIn(ExperimentalForeignApi::class)

package me.emiliomini.dutyschedule.shared.api

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Stores items as generic keychain passwords, readable after the first unlock so the background
 * duty refresh can reach them, and excluded from iCloud backups
 */
class IosSecureStorageApi : PlatformSecureStorageApi {
    private val logger = getPlatformLogger("IosSecureStorageApi")

    companion object {
        const val SERVICE = "me.emiliomini.dutyschedule.credentials"
    }

    override suspend fun get(key: String): String? = memScoped {
        val query = keychainQuery(
            key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        if (status != errSecSuccess) {
            return@memScoped null
        }

        val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
        data.toByteArray().decodeToString()
    }

    override suspend fun set(key: String, value: String) {
        remove(key)

        val bytes = value.encodeToByteArray()
        val data = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }

        val query = keychainQuery(
            key,
            kSecValueData to data,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        )

        val status = SecItemAdd(query, null)
        if (status != errSecSuccess) {
            logger.error("Could not store secret for '$key' (OSStatus $status)")
        }
    }

    override suspend fun remove(key: String) {
        SecItemDelete(keychainQuery(key))
    }

    private fun keychainQuery(key: String, vararg extra: Pair<Any?, Any?>): CFDictionaryRef? {
        val query = mutableMapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE,
            kSecAttrAccount to key
        )
        query.putAll(extra)

        return CFBridgingRetain(query) as? CFDictionaryRef
    }

    private fun NSData.toByteArray(): ByteArray {
        val length = this.length.toInt()
        if (length == 0) {
            return ByteArray(0)
        }
        val bytes = this.bytes ?: return ByteArray(0)
        return bytes.readBytes(length)
    }
}

actual fun initializePlatformSecureStorageApi(): PlatformSecureStorageApi {
    return IosSecureStorageApi()
}
