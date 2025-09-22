package me.emiliomini.dutyschedule.shared.api

interface PlatformRedirectApi {
    fun dialPhone(phone: String)
    fun sendSms(phone: String)
    fun sendWA(phone: String)
    fun sendEmail(email: String)
}

expect fun initializePlatformRedirectApi(): PlatformRedirectApi

private var api: PlatformRedirectApi? = null

fun getPlatformRedirectApi(): PlatformRedirectApi = if (api == null) {
    api = initializePlatformRedirectApi()
    api!!
} else {
    api!!
}
