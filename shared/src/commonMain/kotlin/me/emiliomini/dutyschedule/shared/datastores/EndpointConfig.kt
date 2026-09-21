package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import me.emiliomini.dutyschedule.shared.defaultDocscedUrl
import me.emiliomini.dutyschedule.shared.defaultPrepUrl

/**
 * The service URLs the app talks to. They cannot ship hardcoded, so the user supplies them during
 * onboarding; until then the install falls back to whatever its platform defaults to.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class EndpointConfig(
    @ProtoNumber(1)
    val prepUrl: String = defaultPrepUrl,
    @ProtoNumber(2)
    val docscedUrl: String = defaultDocscedUrl
) : MultiplatformDataModel

fun EndpointConfig.isDefault(): Boolean {
    return this == EndpointConfig()
}
