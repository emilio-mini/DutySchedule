package me.emiliomini.dutyschedule.models.prep

import me.emiliomini.dutyschedule.R

enum class Requirement(val value: String) {
    TIMESLOT("fd4abb09f1cbf2687319798b396cc38255ffb817_2_1544535064_9602"),
    SEW("d590f25129127adfdb7e994be630de152f5f1682_2_1544535093_4117"),
    RTW("390b263970bc93d4612d9a9544d50b1b6bc1d9a7_2_1551891770_4987"),
    ITF("297d898e34f3ce811302fbfd98c238d6adfc5617_2_1668611309_2843"),

    EL("43e3811f89fea7883aa664c53b10f287fdf63020_2_1544535120_1506"),
    TF("6509a03415cb338da9ffa9b2b849cd617bd756ca_2_1544535149_1171"),
    RS("e022f1d19a68909adac66c55ce7adafb520a75ae_2_1544535615_1745"),
    ITF_LKW("72e19bdf669c42d2b8fbfe5be410d082690d2f0a_2_1668611327_4692"),
    ITF_NFS("946e81e89c32695625a890358c9ccb79fec693b6_2_1668611344_1827"),
    RTW_NFS("30b51644b352bb3df6b7c2ce86db5c0b2762d710_2_1551891794_834"),
    RTW_RS("5e5dd6c0ea76308df39e5986aa814b6d3856f24c_2_1707217926_8039"),
    INVALID("");

    fun getResourceString(): Int {
        return when (this) {
            SEW -> R.string.data_requirement_sew
            RTW -> R.string.data_requirement_rtw
            ITF -> R.string.data_requirement_itf
            EL -> R.string.data_requirement_el
            TF -> R.string.data_requirement_tf
            RS -> R.string.data_requirement_rs
            ITF_LKW -> R.string.data_requirement_itf_el
            ITF_NFS -> R.string.data_requirement_itf_nfs
            RTW_NFS -> R.string.data_requirement_rtw_nfs
            RTW_RS -> R.string.data_requirement_rtw_rs
            else -> R.string.data_requirement_none
        }
    }

    companion object {
        const val POSITION = "requirementGroupChildDataGuid"

        fun parse(value: String): Requirement {
            return entries.find { it.value == value } ?: INVALID
        }
    }
}