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
    val alarmOffsetMin: Int = 90,
    @ProtoNumber(4)
    val allowedOrgs: List<String> = emptyList(),
    @ProtoNumber(5)
    val lastSelectedOrg: String = "",
    @ProtoNumber(6)
    val autoSetAlarms: Boolean = false,
    @ProtoNumber(7)
    val permanentNotification: Boolean = true,
    @ProtoNumber(8)
    val backgroundUpdaterEnabled: Boolean = true,
    @ProtoNumber(9)
    val themeMode: Int = 0 // 0 = system, 1 = light, 2 = dark
) : MultiplatformDataModel

fun UserPreferences.isDefault(): Boolean {
    return this == UserPreferences()
}
