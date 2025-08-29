package me.emiliomini.dutyschedule.models.prep

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

    companion object {

        fun parse(value: String): Org {
            return entries.find { it.value == value } ?: INVALID
        }
    }
}