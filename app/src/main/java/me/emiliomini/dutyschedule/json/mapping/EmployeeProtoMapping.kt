package me.emiliomini.dutyschedule.json.mapping

import me.emiliomini.dutyschedule.json.util.JsonMapping
import me.emiliomini.dutyschedule.util.trimLeadingZeros

object EmployeeProtoMapping {
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