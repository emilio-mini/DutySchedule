package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [countsByDefault] is whether the type is ticked in the dashboard's quota filter before the user
 * has touched it, which is to say whether it is service time rather than training or office work
 */
@Serializable
enum class DutyType(val countsByDefault: Boolean) {
    @SerialName("0")
    UNKNOWN(true),
    @SerialName("1")
    EMS(true),
    @SerialName("2")
    TRAINING(false),
    @SerialName("3")
    MEET(false),
    @SerialName("4")
    DRILL(false),
    @SerialName("5")
    VEHICLE_TRAINING(false),
    @SerialName("6")
    RECERTIFICATION(false),
    @SerialName("7")
    HAEND(true),
    @SerialName("8")
    ADMINISTRATIVE(false),
    @SerialName("9")
    EVENT(false),
    @SerialName("10")
    BLOOD_DONATION_SERVICE(false),
    @SerialName("11")
    DRONE_TEAM(true);

    companion object {
        val DEFAULT_COUNTED = entries.filter { it.countsByDefault }.toSet()
    }
}
