package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.json.JsonMapping
import me.emiliomini.dutyschedule.shared.util.trimLeadingZeros

object EmployeeMapping {
    val GUID = JsonMapping.STRING("dataGuid")
    val NAME = JsonMapping.STRING("name")
    val IDENTIFIER = JsonMapping.STRING("personalnummer", transform = { it.trimLeadingZeros() })
    val PHONE = JsonMapping.STRING("telefon")
    val EMAIL = JsonMapping.STRING("email")
    val DEFAULT_ORG = JsonMapping.STRING("externalIsRegularOrgUnit")
    val RESOURCE_TYPE_GUID = JsonMapping.STRING("ressourceTypeDataGuid")
    val BIRTHDATE = JsonMapping.TIMESTAMP("birthdate")

    val STAFF_TO_SKILLS = JsonMapping.ARRAY("staffToSkills")
}