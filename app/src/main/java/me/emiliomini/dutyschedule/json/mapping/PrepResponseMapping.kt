package me.emiliomini.dutyschedule.json.mapping

import me.emiliomini.dutyschedule.json.util.JsonMapping

object PrepResponseMapping {
    val ALERT_MESSAGE = JsonMapping.STRING("alertMessage")
    val CHANGED_DATA_ID = JsonMapping.STRING("changedDataId")
    val DATA_AS_OBJECT = JsonMapping.OBJECT("data")
    val DATA_AS_ARRAY = JsonMapping.ARRAY("data")
    val DATA_COUNT = JsonMapping.INT("dataCount")
    val ERROR_MESSAGES = JsonMapping.ARRAY("errorMessages")
    val SUCCESS_MESSAGE = JsonMapping.STRING("successMessage")
}
