package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * Ties an upcoming duty to the plan entry it stands for. The portal hands out unrelated guids for
 * the two, so the pairing has to be worked out by searching the plans and is kept once found.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DutyLink(
    @ProtoNumber(1)
    val upcomingGuid: String = "",
    @ProtoNumber(2)
    val orgGuid: String = "",
    @ProtoNumber(3)
    val planDutyGuid: String = "",
    @ProtoNumber(4)
    val begin: Timestamp = Timestamp()
) : MultiplatformDataModel

fun DutyLink.isDefault(): Boolean {
    return this == DutyLink()
}
