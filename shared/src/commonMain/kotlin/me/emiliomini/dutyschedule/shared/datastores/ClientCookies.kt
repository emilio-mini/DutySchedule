package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ClientCookies(
    @ProtoNumber(1)
    val clientCookies: Map<String, StoredCookieItems> = emptyMap()
) : MultiplatformDataModel

fun ClientCookies.isDefault(): Boolean {
    return this == ClientCookies()
}
