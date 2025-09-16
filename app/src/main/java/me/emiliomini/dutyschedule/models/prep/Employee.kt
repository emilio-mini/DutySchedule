package me.emiliomini.dutyschedule.models.prep

import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.SkillProto
import me.emiliomini.dutyschedule.util.toTimestamp
import java.time.OffsetDateTime

data class Employee(
    val guid: String,
    val name: String,
    var identifier: String? = null,
    var phone: String = "",
    var email: String = "",
    var defaultOrg: String = "",
    var birthdate: OffsetDateTime? = null,
    var resourceTypeGuid: String = "",
    var skill: MutableList<Skill> = mutableListOf()
) {
    fun toProto(): EmployeeProto {
        val employee = EmployeeProto.newBuilder()
            .setGuid(guid)
            .setName(name)
            .setPhone(phone)
            .setEmail(email)
            .setDefaultOrg(defaultOrg)
            .setResourceTypeGuid(resourceTypeGuid)
            .addAllSkills(skill.map { SkillProto.newBuilder().setGuid(it.value).build() })

        if (identifier != null) {
            employee.setIdentifier(identifier)
        }

        if (birthdate != null) {
            employee.setBirthdate(birthdate!!.toTimestamp())
        }

        return employee.build()
    }

    companion object {
        val KFZ_NAME = "KFZ"
        val VEHICLE_NAME = "DFZ"
        val SEW_NAME = "SEW"
        val ITF_NAME = "ITF"
        val RTW_NAME = "RTW"
        val HAEND_NAME = "HÄND"
        val STAFF_GUID_POSITION = "dataGuid"
        val STAFF_NAME_POSITION = "name"
        val STAFF_IDENTIFIER_POSITION = "personalnummer"

        val GUID_POSITION = "allocationRessourceDataGuid"
        val INFO_POSITION = "info"
        val ADDITIONAL_INFOS_POSITION = "additionalInfos"
        val ADDITIONAL_INFOS_NAME_POSITION = "ressource_name"
        val SKILL_STAFF_POSITION = "staffToSkills"
    }
}
