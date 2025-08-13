package me.emiliomini.dutyschedule.data.models.mapping

enum class Requirement(val value: String) {
    TIMESLOT("fd4abb09f1cbf2687319798b396cc38255ffb817_2_1544535064_9602"),
    SEW("d590f25129127adfdb7e994be630de152f5f1682_2_1544535093_4117"),
    EL("43e3811f89fea7883aa664c53b10f287fdf63020_2_1544535120_1506"),
    TF("6509a03415cb338da9ffa9b2b849cd617bd756ca_2_1544535149_1171"),
    RS("e022f1d19a68909adac66c55ce7adafb520a75ae_2_1544535615_1745"),
    INVALID("");

    companion object {
        val POSITION = "requirementGroupChildDataGuid";

        fun parse(value: String): Requirement {
            return Requirement.entries.find { it.value == value } ?: INVALID;
        }
    }
}