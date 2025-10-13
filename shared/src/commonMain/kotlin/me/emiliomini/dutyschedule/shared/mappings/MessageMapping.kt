@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.json.JsonMapping
import kotlin.time.ExperimentalTime

object MessageMapping {
    val GUID = JsonMapping.STRING("dataGuid")
    val RESOURCE_GUID = JsonMapping.STRING("typeGuid")
    val TITLE = JsonMapping.STRING("title")
    val MESSAGE = JsonMapping.STRING("message")
    val PRIORITY = JsonMapping.INT("messagePriority")
    val DISPLAY_FROM = JsonMapping.INSTANT("displayFrom")
    val DISPLAY_TO = JsonMapping.INSTANT("displayTo")
}