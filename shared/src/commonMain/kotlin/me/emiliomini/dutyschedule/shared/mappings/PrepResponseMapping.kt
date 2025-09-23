package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.json.JsonMapping

object PrepResponseMapping {
    val ALERT_MESSAGE = JsonMapping.STRING("alertMessage")
    val CHANGED_DATA_ID = JsonMapping.STRING("changedDataId")
    val DATA_AS_OBJECT = JsonMapping.OBJECT("data")
    val DATA_AS_ARRAY = JsonMapping.ARRAY("data")
    val DATA_AS_ARRAY_OR_OBJECT = JsonMapping.ARRAY_OR_OBJECT("data")
    val DATA_COUNT = JsonMapping.INT("dataCount")
    val ERROR_MESSAGES = JsonMapping.ARRAY("errorMessages")
    val SUCCESS_MESSAGE = JsonMapping.STRING("successMessage")
}
