package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The service URLs the app talks to. They cannot ship hardcoded, so the user supplies them during
 * onboarding; the defaults here are what the app used to be built with.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class EndpointConfig(
    @ProtoNumber(1)
    val prepUrl: String = DEFAULT_PREP_URL,
    @ProtoNumber(2)
    val docscedUrl: String = DEFAULT_DOCSCED_URL
) : MultiplatformDataModel {
    companion object {
        const val DEFAULT_PREP_URL = "https://dienstplan.o.roteskreuz.at"
        const val DEFAULT_DOCSCED_URL = "https://docsced.app"
    }
}

fun EndpointConfig.isDefault(): Boolean {
    return this == EndpointConfig()
}
