package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.json.JsonMapping

object DutyDefinitionMapping {
    val GUID = JsonMapping.STRING("dataGuid")
    val PARENT_GUID = JsonMapping.STRING("parentDataGuid")
    val TYPE = JsonMapping.INT("type")
    val BEGIN = JsonMapping.TIMESTAMP("begin")
    val END = JsonMapping.TIMESTAMP("end")
    val INFO = JsonMapping.STRING("info")
    val EMPLOYEE_GUID = JsonMapping.STRING("allocationRessourceDataGuid")
    val INLINE_NAME = JsonMapping.STRING("additionalInfos", "ressource_name")
    val REQUIREMENT = JsonMapping.STRING("requirementGroupChildDataGuid")
    val RESOURCE_TYPE_GUID = JsonMapping.STRING("ressourceTypeDataGuid")
}