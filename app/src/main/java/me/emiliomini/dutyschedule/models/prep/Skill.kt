package me.emiliomini.dutyschedule.models.prep

import me.emiliomini.dutyschedule.R

enum class Skill(val value: String) {

    RS("916dfa675111ff0378e15e83c4daa2ea59d043ed_2_1544534410_948"),
    AZUBI("e87b6976147e38a96a1e682ab1ef82600108a89c_2_1566370936_4014"),
    NFS("e87b6976147e38a96a1e682ab1ef82600108a89c_2_1566370936_4014"),
    INVALID("");

    fun getResourceString(): Int {
        return when (this) {
            RS -> R.string.data_skill_rs
            AZUBI -> R.string.data_skill_azubi
            NFS -> R.string.data_skill_nfs
            else -> R.string.data_requirement_none
        }
    }

    companion object {
        const val POSITION = "skillDataGuid"

        fun parse(value: String): Skill {
            return entries.find { it.value == value } ?: INVALID
        }
    }
}