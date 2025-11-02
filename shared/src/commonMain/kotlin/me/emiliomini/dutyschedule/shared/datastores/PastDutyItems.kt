package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PastDutyItems(
    @ProtoNumber(1)
    val years: Map<Int, YearlyDutyItems> = emptyMap()
) : MultiplatformDataModel

fun PastDutyItems.isDefault(): Boolean {
    return this == PastDutyItems()
}
