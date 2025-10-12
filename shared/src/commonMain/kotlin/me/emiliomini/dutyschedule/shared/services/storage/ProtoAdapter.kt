package me.emiliomini.dutyschedule.shared.services.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
class ProtoAdapter<T>(private val kSerializer: KSerializer<T>) {

    val defaultValue: T
        get() = ProtoBuf.decodeFromByteArray(kSerializer, ByteArray(0))

    suspend fun fromBytes(bytes: ByteArray): T =
        withContext(Dispatchers.Default) {
            ProtoBuf.decodeFromByteArray(kSerializer, bytes)
        }

    suspend fun toBytes(value: T): ByteArray =
        withContext(Dispatchers.Default) {
            ProtoBuf.encodeToByteArray(kSerializer, value)
        }
}