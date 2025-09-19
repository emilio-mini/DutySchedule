package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Incode(
    @ProtoNumber(1)
    val token: String = "",
    @ProtoNumber(2)
    val value: String = "",
    @ProtoNumber(3)
    val lastUsed: Timestamp = Timestamp()
) : MultiplatformDataModel
