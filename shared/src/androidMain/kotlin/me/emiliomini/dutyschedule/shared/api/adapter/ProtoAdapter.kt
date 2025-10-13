package me.emiliomini.dutyschedule.shared.api.adapter

import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
class ProtoAdapter<T>(private val kSerializer: KSerializer<T>) : Serializer<T> {

    override val defaultValue: T
        get() = ProtoBuf.decodeFromByteArray(kSerializer, ByteArray(0))

    override suspend fun readFrom(input: InputStream): T =
        ProtoBuf.decodeFromByteArray(kSerializer, input.readBytes())

    override suspend fun writeTo(t: T, output: OutputStream) {
        val bytes = ProtoBuf.encodeToByteArray(kSerializer, t)
        output.write(bytes)
    }
}