package me.emiliomini.dutyschedule.json.mapping

import me.emiliomini.dutyschedule.json.util.JsonMapping

object DutyDefinitionProtoMapping {
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