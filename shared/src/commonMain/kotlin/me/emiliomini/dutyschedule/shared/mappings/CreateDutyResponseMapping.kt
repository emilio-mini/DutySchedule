@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.json.JsonMapping
import kotlin.time.ExperimentalTime

object CreateDutyResponseMapping {
    val GUID = JsonMapping.STRING("guid")
    val DATA_GUID = JsonMapping.STRING("dataGuid")
    val ORG = JsonMapping.STRING("orgUnitDataGuid")
    val BEGIN = JsonMapping.INSTANT("begin")
    val END = JsonMapping.INSTANT("end")
    val REQUIREMENT_GROUP_CHILD_DATA_GUID = JsonMapping.STRING("requirementGroupChildDataGuid")
    val RESOURCE_TYPE_DATA_GUID = JsonMapping.STRING("ressourceTypeDataGuid")
    val SKILL_DATA_GUID = JsonMapping.STRING("skillDataGuid")
    val SKILL_CHARACTERISATION_DATA_GUID = JsonMapping.STRING("skillCharacterisationDataGuid")
    val SHIFT_DATA_GUID = JsonMapping.STRING("shiftDataGuid")
    val PLAN_BASE_DATA_GUID = JsonMapping.STRING("planBaseDataGuid")
    val PLAN_BASE_ENTRY_DATA_GUID = JsonMapping.STRING("planBaseEntryDataGuid")
    val ALLOCATION_DATA_GUID = JsonMapping.STRING("allocationDataGuid")
    val ALLOCATION_RESOURCE_DATA_GUID = JsonMapping.STRING("allocationRessourceDataGuid")
    val RELEASED = JsonMapping.INT("released")
    val BOOKABLE = JsonMapping.INT("bookable")
    val RESOURCE_NAME = JsonMapping.STRING("additionalInfos", "ressource_name")
}