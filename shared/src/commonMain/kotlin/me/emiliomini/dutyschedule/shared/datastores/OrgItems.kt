package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OrgItems(
    @ProtoNumber(1)
    val orgs: Map<String, Org> = emptyMap()
) : MultiplatformDataModel
