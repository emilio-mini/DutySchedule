@file:OptIn(
    ExperimentalForeignApi::class,
    ExperimentalSerializationApi::class,
    BetaInteropApi::class
)

package me.emiliomini.dutyschedule.shared.api

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import me.emiliomini.dutyschedule.shared.datastores.MultiplatformDataModel
import me.emiliomini.dutyschedule.shared.services.storage.MultiplatformDataStore
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile

/**
 * Must match the App Group entitlement on both the host app and the widget extension targets
 * (see `dutyscheduleIOS.entitlements`). Data is stored here rather than in the app's private
 * Documents directory so the WidgetKit extension process can read it too.
 */
private const val APP_GROUP_ID = "group.me.emiliomini.dutyscheduleIOS"

class IosStorageApi : PlatformStorageApi {
    private val logger = getPlatformLogger("IosStorageApi")
    private val fileManager = NSFileManager.defaultManager

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun initialize(stores: List<MultiplatformDataStore<out MultiplatformDataModel>>) {
        val directory = getStorageDirectoryPath()
        if (!fileManager.fileExistsAtPath(directory)) {
            fileManager.createDirectoryAtPath(
                directory,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
    }

    override suspend fun <T : MultiplatformDataModel> get(
        store: MultiplatformDataStore<T>
    ): T? {
        val path = getFilePath(store)
        if (!fileManager.fileExistsAtPath(path)) {
            return null
        }

        val bytes = NSData.dataWithContentsOfFile(path)?.toByteArray()
        return if (bytes == null) null else ProtoBuf.decodeFromByteArray(store.serializer, bytes)
    }

    override suspend fun <T : MultiplatformDataModel> update(
        store: MultiplatformDataStore<T>, newData: T?
    ) {
        val data: ByteArray =
            ProtoBuf.encodeToByteArray(store.serializer, newData ?: store.defaultValue)
        return data.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = data.size.toULong()
            ).writeToFile(getFilePath(store), atomically = true)
        }
    }

    private fun getFilePath(store: MultiplatformDataStore<*>): String {
        return "${getStorageDirectoryPath()}/${store.id}.dat"
    }

    private fun getStorageDirectoryPath(): String {
        val containerUrl = fileManager.containerURLForSecurityApplicationGroupIdentifier(APP_GROUP_ID)
        val containerPath = containerUrl?.path
        if (containerPath == null) {
            logger.warn(
                "App Group '$APP_GROUP_ID' container unavailable (missing entitlement?); " +
                    "falling back to app-private storage. Widgets will not see this data."
            )
            return getDocumentsDirectoryPath()
        }
        return "$containerPath/Data"
    }

    private fun getDocumentsDirectoryPath(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true
        )
        return paths.first() as String
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

actual fun initializePlatformStorageApi(): PlatformStorageApi {
    return IosStorageApi()
}
