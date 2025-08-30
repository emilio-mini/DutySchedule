package me.emiliomini.dutyschedule.services.storage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Parser
import java.io.InputStream
import java.io.OutputStream

class ProtoSerializer<T : com.google.protobuf.MessageLite>(
    private val parser: Parser<T>,
    private val defaultInstance: T
) : Serializer<T> {

    override val defaultValue: T
        get() = defaultInstance

    override suspend fun readFrom(input: InputStream): T = try {
        parser.parseFrom(input)
    } catch (e: InvalidProtocolBufferException) {
        throw CorruptionException("Failed to parse protobuf.", e)
    }

    override suspend fun writeTo(t: T, output: OutputStream) = t.writeTo(output)
}
