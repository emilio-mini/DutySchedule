package me.emiliomini.dutyschedule.shared.datastores

import io.ktor.http.Cookie
import io.ktor.util.date.GMTDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class StoredCookie(
    @ProtoNumber(1)
    val name: String,
    @ProtoNumber(2)
    val value: String,
    @ProtoNumber(3)
    val expires: Long?,
    @ProtoNumber(4)
    val maxAge: Int?,
    @ProtoNumber(5)
    val domain: String?,
    @ProtoNumber(6)
    val path: String?,
    @ProtoNumber(7)
    val secure: Boolean,
    @ProtoNumber(8)
    val httpOnly: Boolean,
    @ProtoNumber(9)
    val extensions: Map<String, String?>? = null
) : MultiplatformDataModel {
    companion object Companion {
        fun fromKtor(cookie: Cookie): StoredCookie = StoredCookie(
            name = cookie.name,
            value = cookie.value,
            expires = cookie.expires?.timestamp,
            maxAge = cookie.maxAge,
            domain = cookie.domain,
            path = cookie.path,
            secure = cookie.secure,
            httpOnly = cookie.httpOnly,
            extensions = cookie.extensions.takeIf { it.isNotEmpty() }
        )

        fun toKtor(sc: StoredCookie): Cookie = Cookie(
            name = sc.name,
            value = sc.value,
            expires = sc.expires?.let { if (it != 0L) GMTDate(it) else null },
            maxAge = sc.maxAge,
            domain = sc.domain,
            path = sc.path,
            secure = sc.secure,
            httpOnly = sc.httpOnly,
            extensions = sc.extensions.orEmpty()
        )
    }
}