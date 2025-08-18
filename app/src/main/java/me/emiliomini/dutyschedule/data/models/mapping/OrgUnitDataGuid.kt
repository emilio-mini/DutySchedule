package me.emiliomini.dutyschedule.data.models.mapping

import me.emiliomini.dutyschedule.R

enum class OrgUnitDataGuid(val value: String) {
    AMBULANCES_UPPER_AUSTRIA("7073d5b6139346f79fb0fa8e0db4a776d3efd300_2_1551802320_6606"),
    EMS_SATTLEDT("1b34355dc0b54be6560e42222d11ce9c6efca21d_2_1551802942_791"),
    EMS_WELS("7b8165e3a860f1f0cd7a99045c8cfb9b4923b226_2_1551802890_0363"),
    TRAINING_WELS("134d54696293dde188108ab0c00eefb3670dd185_2_1551857013_4155"),
    HAEND_WELS_CITY("11d43db06551ef61d472076f222802ca02cd1383_2_1551803241_1914"),
    HAEND_WELS_SURROUNDINGS("60d9b5e93c24feced17c513fb3f95c18cd828abe_2_1551803338_4809");

    fun getResourceString(): Int {
        return when (this) {
            AMBULANCES_UPPER_AUSTRIA -> R.string.data_station_ambulances_upper_austria
            EMS_SATTLEDT -> R.string.data_station_sattledt
            EMS_WELS -> R.string.data_station_wels
            TRAINING_WELS -> R.string.data_station_training_wels
            HAEND_WELS_CITY -> R.string.data_station_haend_wels_city
            HAEND_WELS_SURROUNDINGS -> R.string.data_station_haend_wels_surroundings
        }
    }
}