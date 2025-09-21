package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.json.JsonMapping

object DutyGroupMapping {
    val GUID = JsonMapping.STRING("dataGuid")
    val TITLE = JsonMapping.STRING("description")
}