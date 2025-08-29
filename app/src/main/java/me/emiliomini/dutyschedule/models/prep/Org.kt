package me.emiliomini.dutyschedule.models.prep

import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.models.prep.Requirement.EL
import me.emiliomini.dutyschedule.models.prep.Requirement.HAEND
import me.emiliomini.dutyschedule.models.prep.Requirement.HAEND_DR
import me.emiliomini.dutyschedule.models.prep.Requirement.HAEND_EL
import me.emiliomini.dutyschedule.models.prep.Requirement.ITF
import me.emiliomini.dutyschedule.models.prep.Requirement.ITF_LKW
import me.emiliomini.dutyschedule.models.prep.Requirement.ITF_NFS
import me.emiliomini.dutyschedule.models.prep.Requirement.RS
import me.emiliomini.dutyschedule.models.prep.Requirement.RTW
import me.emiliomini.dutyschedule.models.prep.Requirement.RTW_NFS
import me.emiliomini.dutyschedule.models.prep.Requirement.RTW_RS
import me.emiliomini.dutyschedule.models.prep.Requirement.SEW
import me.emiliomini.dutyschedule.models.prep.Requirement.TF

enum class Org(val value: String) {

    WELS_STADT("11d43db06551ef61d472076f222802ca02cd1383_2_1551803241_1914"),
    WELS_LAND("60d9b5e93c24feced17c513fb3f95c18cd828abe_2_1551803338_4809"),

    INVALID("");

    fun getDocscedConfig(): String {
        return when (this) {
            WELS_STADT -> "welsstadt"
            WELS_LAND -> "welsland"
            else -> "welsstadt"
        }
    }

    fun getResourceString(): Int {
        return when (this) {
            WELS_STADT -> R.string.data_org_we
            WELS_LAND -> R.string.data_org_wl
            else -> R.string.data_requirement_none
        }
    }

    companion object {

        fun parse(value: String): Org {
            return entries.find { it.value == value } ?: INVALID
        }
    }
}