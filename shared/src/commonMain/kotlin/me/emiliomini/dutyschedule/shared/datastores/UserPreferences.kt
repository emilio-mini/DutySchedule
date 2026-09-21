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
    /**
     * Which duty types the dashboard quota counts. Held alongside [countedDutyTypesConfigured] so
     * that clearing every tick stays distinguishable from never having opened the filter
     */
    @ProtoNumber(7)
    val countedDutyTypesConfigured: Boolean = false,
    @ProtoNumber(8)
    val countedDutyTypes: List<DutyType> = emptyList()
) : MultiplatformDataModel

/** The ticked types, falling back to the per type defaults until the user picks their own */
fun UserPreferences.countedDutyTypes(): Set<DutyType> {
    return if (this.countedDutyTypesConfigured) {
        this.countedDutyTypes.toSet()
    } else {
        DutyType.DEFAULT_COUNTED
    }
}

fun UserPreferences.isDefault(): Boolean {
    return this == UserPreferences()
}
