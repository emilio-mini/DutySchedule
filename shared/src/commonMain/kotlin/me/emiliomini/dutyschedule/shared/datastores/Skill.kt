package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Skill(
    @ProtoNumber(1)
    val guid: String = ""
) : MultiplatformDataModel

fun Skill.isDefault(): Boolean {
    return this == Skill()
}
