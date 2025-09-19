package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Employee(
    @ProtoNumber(1)
    val guid: String,
    @ProtoNumber(2)
    val name: String,
    @ProtoNumber(3)
    val identifier: String?,
    @ProtoNumber(4)
    val phone: String?,
    @ProtoNumber(5)
    val email: String?,
    @ProtoNumber(6)
    val defaultOrg: String?,
    @ProtoNumber(7)
    val birthdate: Timestamp?,
    @ProtoNumber(8)
    val resourceTypeGuid: String,
    @ProtoNumber(9)
    val skills: List<Skill> = emptyList()
)
