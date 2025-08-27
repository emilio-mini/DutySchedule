package me.emiliomini.dutyschedule.models.prep

import java.time.OffsetDateTime

data class Employee(
    val guid: String,
    val name: String,
    var identifier: String? = null,
    var phone: String = "",
    var email: String = "",
    var defaultOrg: String = "",
    var birthdate: OffsetDateTime? = null,
    var resourceTypeGuid: String = ""
) {
    companion object {
        val SEW_NAME = "SEW"
        val ITF_NAME = "ITF"
        val RTW_NAME = "RTW"

        val STAFF_GUID_POSITION = "dataGuid"
        val STAFF_NAME_POSITION = "name"
        val STAFF_IDENTIFIER_POSITION = "personalnummer"

        val GUID_POSITION = "allocationRessourceDataGuid"
        val INFO_POSITION = "info"
        val ADDITIONAL_INFOS_POSITION = "additionalInfos"
        val ADDITIONAL_INFOS_NAME_POSITION = "ressource_name"
    }
}
