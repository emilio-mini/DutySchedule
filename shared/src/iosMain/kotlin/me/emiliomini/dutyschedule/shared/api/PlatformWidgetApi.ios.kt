package me.emiliomini.dutyschedule.shared.api

/**
 * Set via [IosPlatformBridge.widgetRefreshHook] from Swift at app launch. WidgetKit is a
 * Swift-only framework with no Objective-C header, so Kotlin/Native cinterop can't call it
 * directly - this hook is the bridge. Not public: Swift should go through [IosPlatformBridge].
 */
internal var iosWidgetRefreshHook: (() -> Unit)? = null

class IosWidgetApi : PlatformWidgetApi {
    override suspend fun refreshWidgets() {
        iosWidgetRefreshHook?.invoke()
    }
}

actual fun initializePlatformWidgetApi(): PlatformWidgetApi = IosWidgetApi()
