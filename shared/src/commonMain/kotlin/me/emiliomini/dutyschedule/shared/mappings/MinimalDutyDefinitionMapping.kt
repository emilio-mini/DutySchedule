package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.json.JsonMapping
import me.emiliomini.dutyschedule.shared.json.JsonMappingPathVariables

object MinimalDutyDefinitionMapping {
    val GUID = JsonMapping.STRING("guid")
    val BEGIN = JsonMapping.TIMESTAMP("begin")
    val END = JsonMapping.TIMESTAMP("end")
    val DURATION = JsonMapping.INT("duration")
    val ALLOCATION_INFO = JsonMapping.ARRAY("allocationInfo", JsonMappingPathVariables.FIRST_KEY)
}