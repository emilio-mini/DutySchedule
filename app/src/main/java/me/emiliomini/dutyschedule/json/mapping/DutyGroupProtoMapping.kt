package me.emiliomini.dutyschedule.json.mapping

import me.emiliomini.dutyschedule.json.util.JsonMapping

object DutyGroupProtoMapping {
    val GUID = JsonMapping.STRING("dataGuid")
    val TITLE = JsonMapping.STRING("description")
}