package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class EmployeeItems(
    @ProtoNumber(1)
    val employees: Map<String, Employee> = emptyMap()
) : MultiplatformDataModel
