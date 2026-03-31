package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OrgTimeline(
    @ProtoNumber(1)
    val guid: String = "",
    @ProtoNumber(2)
    val timeline: List<OrgDay> = emptyList()
) : MultiplatformDataModel

fun OrgTimeline.isDefault(): Boolean {
    return this == OrgTimeline()
}
