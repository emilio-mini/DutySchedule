package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * Served minutes of the tracked year, split by duty type so the dashboard can total up only the
 * types the user cares about. Field 1 held the single combined figure this replaced
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Statistics(
    @ProtoNumber(2)
    val minutesByDutyType: Map<DutyType, Int> = emptyMap()
) : MultiplatformDataModel

fun Statistics.isDefault(): Boolean {
    return this == Statistics()
}

/** Minutes served across every duty type, regardless of what the quota filter has ticked */
fun Statistics.totalMinutes(): Int {
    return this.minutesByDutyType.values.sum()
}

fun Statistics.minutesOf(types: Set<DutyType>): Int {
    return this.minutesByDutyType.filterKeys { types.contains(it) }.values.sum()
}
