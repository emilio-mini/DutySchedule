package me.emiliomini.dutyschedule.shared.api

interface PlatformWidgetApi {
    /** Requests an immediate refresh of any placed home-screen widgets. */
    suspend fun refreshWidgets()
}

expect fun initializePlatformWidgetApi(): PlatformWidgetApi

private var api: PlatformWidgetApi? = null

fun getPlatformWidgetApi(): PlatformWidgetApi = if (api == null) {
    api = initializePlatformWidgetApi()
    api!!
} else {
    api!!
}
