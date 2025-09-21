package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.json.JsonMapping

object ResourceMapping {
    val EMPLOYEE_GUID = JsonMapping.STRING("ressourceTypeDataGuid")
    val MESSAGES_GUID = JsonMapping.STRING("dataGuid")
}