package me.emiliomini.dutyschedule.json.mapping

import me.emiliomini.dutyschedule.json.util.JsonMapping

object OrgProtoMapping {
    val TITLE = JsonMapping.STRING("additionalInfos", "orgUnitName")
    val ABBREVIATION = JsonMapping.STRING("additionalInfos", "orgUnitAbbreviation")
    val IDENTIFIER = JsonMapping.STRING("additionalInfos", "externalId")
}