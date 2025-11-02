package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class YearlyDutyItems(
    @ProtoNumber(1)
    val minimalDutyDefinitions: List<MinimalDutyDefinition> = emptyList(),
    @ProtoNumber(2)
    val year: Int = 2000
) : MultiplatformDataModel

fun YearlyDutyItems.isDefault(): Boolean {
    return this == YearlyDutyItems()
}
