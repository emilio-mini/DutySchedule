package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Timestamp(
    @ProtoNumber(1)
    val seconds: Long,
    @ProtoNumber(2)
    val nanos: Int
)
