package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MinimalDutyDefinition(
    @ProtoNumber(1)
    val guid: String = "",
    @ProtoNumber(2)
    val begin: Timestamp = Timestamp(),
    @ProtoNumber(3)
    val end: Timestamp = Timestamp(),
    @ProtoNumber(4)
    val type: DutyType = DutyType.UNKNOWN,
    @ProtoNumber(5)
    val vehicle: String? = null,
    @ProtoNumber(6)
    val staff: List<String> = emptyList(),
    @ProtoNumber(7)
    val duration: Int = 0,
    @ProtoNumber(8)
    val typeString: String = ""
) : MultiplatformDataModel

fun MinimalDutyDefinition.isDefault(): Boolean {
    return this == MinimalDutyDefinition()
}
