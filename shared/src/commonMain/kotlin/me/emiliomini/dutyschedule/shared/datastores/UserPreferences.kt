package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class UserPreferences(
    @ProtoNumber(1)
    val username: String = "",
    @ProtoNumber(2)
    val password: String = "",
    @ProtoNumber(3)
    val alarmOffsetMin: Int = 0,
    @ProtoNumber(4)
    val allowedOrgs: List<String> = emptyList()
) : MultiplatformDataModel
