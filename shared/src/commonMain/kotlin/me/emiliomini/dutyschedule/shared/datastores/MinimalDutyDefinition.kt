package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class MinimalDutyDefinition(
    @ProtoNumber(1)
    val guid: String,
    @ProtoNumber(2)
    val begin: Timestamp,
    @ProtoNumber(3)
    val end: Timestamp,
    @ProtoNumber(4)
    val type: DutyType,
    @ProtoNumber(5)
    val vehicle: String?,
    @ProtoNumber(6)
    val staff: List<String> = emptyList(),
    @ProtoNumber(7)
    val duration: Int,
    @ProtoNumber(8)
    val typeString: String
)
