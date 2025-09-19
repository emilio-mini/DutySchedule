package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Alarm(
    @ProtoNumber(1)
    val active: Boolean = false,
    @ProtoNumber(2)
    val timestamp: Long = 0,
    @ProtoNumber(3)
    val code: Int = 0
) : MultiplatformDataModel
