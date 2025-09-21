package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Employee(
    @ProtoNumber(1)
    val guid: String = "",
    @ProtoNumber(2)
    val name: String = "",
    @ProtoNumber(3)
    val identifier: String? = null,
    @ProtoNumber(4)
    val phone: String? = null,
    @ProtoNumber(5)
    val email: String? = null,
    @ProtoNumber(6)
    val defaultOrg: String? = null,
    @ProtoNumber(7)
    val birthdate: Timestamp? = null,
    @ProtoNumber(8)
    val resourceTypeGuid: String = "",
    @ProtoNumber(9)
    val skills: List<Skill> = emptyList()
) : MultiplatformDataModel {
    companion object {
        val KFZ_NAME = "KFZ"
        val VEHICLE_NAME = "DFZ"
        val SEW_NAME = "SEW"
        val ITF_NAME = "ITF"
        val RTW_NAME = "RTW"
        val HAEND_NAME = "HÄND"
    }
}

fun Employee.isDefault(): Boolean {
    return this == Employee()
}

