package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DutyDefinition(
    @ProtoNumber(1)
    val guid: String,
    @ProtoNumber(2)
    val begin: Timestamp,
    @ProtoNumber(3)
    val end: Timestamp,
    @ProtoNumber(4)
    val slots: List<Slot> = emptyList(),
    @ProtoNumber(5)
    val info: String?,
    @ProtoNumber(6)
    val groupGuid: String?
)
