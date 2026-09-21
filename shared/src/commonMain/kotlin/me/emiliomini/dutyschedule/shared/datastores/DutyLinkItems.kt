package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/** Resolved [DutyLink]s by the guid of the upcoming duty they were resolved for */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DutyLinkItems(
    @ProtoNumber(1)
    val links: Map<String, DutyLink> = emptyMap()
) : MultiplatformDataModel

fun DutyLinkItems.isDefault(): Boolean {
    return this == DutyLinkItems()
}
