package me.emiliomini.dutyschedule.json.mapping

import me.emiliomini.dutyschedule.json.util.JsonMapping

object MessageMapping {
    val GUID = JsonMapping.STRING("dataGuid")
    val RESOURCE_GUID = JsonMapping.STRING("typeGuid")
    val TITLE = JsonMapping.STRING("title")
    val MESSAGE = JsonMapping.STRING("message")
    val PRIORITY = JsonMapping.INT("messagePriority")
    val DISPLAY_FROM = JsonMapping.OFFSET_DATE_TIME("displayFrom")
    val DISPLAY_TO = JsonMapping.OFFSET_DATE_TIME("displayTo")
}