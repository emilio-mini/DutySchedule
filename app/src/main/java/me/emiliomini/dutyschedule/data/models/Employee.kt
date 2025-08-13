package me.emiliomini.dutyschedule.data.models

data class Employee(
    val guid: String,
    val name: String,
    var identifier: String? = null,
    var phone: String? = null
) {
    companion object {
        val SEW_NAME = "SEW";

        val DEFAULT_IDENTIFIER = "00000000";

        val STAFF_GUID_POSITION = "dataGuid";
        val STAFF_NAME_POSITION = "name";
        val STAFF_IDENTIFIER_POSITION = "personalnummer";

        val GUID_POSITION = "allocationRessourceDataGuid";
        val INFO_POSITION = "info";
        val ADDITIONAL_INFOS_POSITION = "additionalInfos";
        val ADDITIONAL_INFOS_NAME_POSITION = "ressource_name"
    }
}
