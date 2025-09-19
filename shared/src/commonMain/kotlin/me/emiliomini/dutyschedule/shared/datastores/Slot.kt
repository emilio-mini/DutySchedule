package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Slot(
    @ProtoNumber(1)
    val guid: String,
    @ProtoNumber(2)
    val employeeGuid: String?,
    @ProtoNumber(3)
    val requirement: Requirement,
    @ProtoNumber(4)
    val begin: Timestamp,
    @ProtoNumber(5)
    val end: Timestamp,
    @ProtoNumber(6)
    val info: String?,
    @ProtoNumber(7)
    val inlineEmployee: Employee?
)
