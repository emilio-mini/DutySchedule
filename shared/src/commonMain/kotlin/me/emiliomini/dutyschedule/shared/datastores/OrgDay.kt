package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OrgDay(
    @ProtoNumber(1)
    val orgGuid: String = "",
    @ProtoNumber(2)
    val date: Timestamp = Timestamp(),
    @ProtoNumber(3)
    val lastUpdated: Timestamp = Timestamp(),
    @ProtoNumber(4)
    val dayShifts: List<DutyDefinition> = emptyList(),
    @ProtoNumber(5)
    val nightShift: List<DutyDefinition> = emptyList(),
    @ProtoNumber(6)
    val groups: List<DutyGroup> = emptyList()
) : MultiplatformDataModel
