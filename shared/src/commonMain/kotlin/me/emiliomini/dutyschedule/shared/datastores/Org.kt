package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Org(
    @ProtoNumber(1)
    val guid: String,
    @ProtoNumber(2)
    val title: String,
    @ProtoNumber(3)
    val abbreviation: String,
    @ProtoNumber(4)
    val identifier: String
)
