package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.json.JsonMapping

object OrgMapping {
    val TITLE = JsonMapping.STRING("additionalInfos", "orgUnitName")
    val ABBREVIATION = JsonMapping.STRING("additionalInfos", "orgUnitAbbreviation")
    val IDENTIFIER = JsonMapping.STRING("additionalInfos", "externalId")
}