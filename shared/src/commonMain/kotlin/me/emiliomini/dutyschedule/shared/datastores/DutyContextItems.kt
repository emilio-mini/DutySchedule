package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/** Resolved [DutyContext]s by the guid of the upcoming duty they belong to */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DutyContextItems(
    @ProtoNumber(1)
    val contexts: Map<String, DutyContext> = emptyMap()
) : MultiplatformDataModel

fun DutyContextItems.isDefault(): Boolean {
    return this == DutyContextItems()
}
