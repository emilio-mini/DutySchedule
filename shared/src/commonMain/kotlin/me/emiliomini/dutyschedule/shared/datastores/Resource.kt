package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Resource(
    @ProtoNumber(0)
    val employeeGuid: String,
    @ProtoNumber(1)
    val messagesGuid: String
)
