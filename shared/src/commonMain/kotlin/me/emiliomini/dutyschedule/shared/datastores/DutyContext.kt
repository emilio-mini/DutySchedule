package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * An upcoming duty as it stands in the plan, with the vehicle handovers around it. [handoverFrom]
 * and [handoverTo] are the duties the same vehicle runs immediately before and after this one, so
 * a null on either side means the vehicle comes out of, or goes back to, the garage.
 *
 * Kept on disk because working it out costs a plan request, and a roster that is a few minutes old
 * beats an empty card while one is in flight.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DutyContext(
    @ProtoNumber(1)
    val upcomingGuid: String = "",
    @ProtoNumber(2)
    val orgGuid: String = "",
    @ProtoNumber(3)
    val duty: DutyDefinition = DutyDefinition(),
    @ProtoNumber(4)
    val vehicle: Slot? = null,
    @ProtoNumber(5)
    val handoverFrom: DutyDefinition? = null,
    @ProtoNumber(6)
    val handoverTo: DutyDefinition? = null,
    @ProtoNumber(7)
    val refreshedAt: Timestamp = Timestamp()
) : MultiplatformDataModel

fun DutyContext.isDefault(): Boolean {
    return this == DutyContext()
}
