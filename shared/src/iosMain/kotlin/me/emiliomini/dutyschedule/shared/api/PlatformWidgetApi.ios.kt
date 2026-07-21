package me.emiliomini.dutyschedule.shared.api

class IosWidgetApi : PlatformWidgetApi {
    override suspend fun refreshWidgets() {
        // No home-screen widgets implemented on iOS yet.
    }
}

actual fun initializePlatformWidgetApi(): PlatformWidgetApi = IosWidgetApi()
