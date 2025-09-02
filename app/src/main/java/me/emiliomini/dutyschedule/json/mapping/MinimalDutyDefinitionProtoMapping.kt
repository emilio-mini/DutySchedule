package me.emiliomini.dutyschedule.json.mapping

import me.emiliomini.dutyschedule.json.util.JsonMapping
import me.emiliomini.dutyschedule.json.util.JsonMappingPathVariables

object MinimalDutyDefinitionProtoMapping {
    val GUID = JsonMapping.STRING("guid")
    val BEGIN = JsonMapping.TIMESTAMP("begin")
    val END = JsonMapping.TIMESTAMP("end")
    val DURATION = JsonMapping.INT("duration")
    val ALLOCATION_INFO = JsonMapping.ARRAY("allocationInfo", JsonMappingPathVariables.FIRST_KEY)
}