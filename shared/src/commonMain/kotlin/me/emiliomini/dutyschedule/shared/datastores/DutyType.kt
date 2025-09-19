package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DutyType {
    @SerialName("0")
    UNKNOWN,
    @SerialName("1")
    EMS,
    @SerialName("2")
    TRAINING,
    @SerialName("3")
    MEET,
    @SerialName("4")
    DRILL,
    @SerialName("5")
    VEHICLE_TRAINING,
    @SerialName("6")
    RECERTIFICATION,
    @SerialName("7")
    HAEND,
    @SerialName("8")
    ADMINISTRATIVE
}