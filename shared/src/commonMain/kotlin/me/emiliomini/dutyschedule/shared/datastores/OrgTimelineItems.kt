package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OrgTimelineItems(
    @ProtoNumber(1)
    val orgTimelines: Map<String, OrgTimeline> = emptyMap()
) : MultiplatformDataModel

fun OrgTimelineItems.isDefault(): Boolean {
    return this == OrgTimelineItems()
}
