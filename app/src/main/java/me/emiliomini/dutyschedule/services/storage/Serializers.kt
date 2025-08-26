package me.emiliomini.dutyschedule.services.storage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import me.emiliomini.dutyschedule.datastore.alarm.AlarmItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import java.io.InputStream
import java.io.OutputStream

object AlarmProtoSerializer : Serializer<AlarmItemsProto> {

    override val defaultValue: AlarmItemsProto
        get() = AlarmItemsProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AlarmItemsProto {
        try {
            return AlarmItemsProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AlarmItemsProto, output: OutputStream) = t.writeTo(output)

}

object OrgsProtoSerializer : Serializer<OrgItemsProto> {

    override val defaultValue: OrgItemsProto
        get() = OrgItemsProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): OrgItemsProto {
        try {
            return OrgItemsProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: OrgItemsProto, output: OutputStream) = t.writeTo(output)

}
