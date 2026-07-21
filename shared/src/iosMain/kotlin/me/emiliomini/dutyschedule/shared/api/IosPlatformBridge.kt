package me.emiliomini.dutyschedule.shared.api

/**
 * Single, deliberately Swift-friendly entry point for the handful of things that must be wired
 * up from the iOS app target (AppDelegate) rather than pure Kotlin/Native cinterop - either
 * because Apple requires the call to happen at a specific point in the app lifecycle
 * (BGTaskScheduler registration must happen before the app finishes launching), or because the
 * framework involved is Swift-only with no Objective-C header for Kotlin/Native to bind against
 * (WidgetKit's WidgetCenter).
 *
 * Kotlin `object`s export to Swift with a stable, predictable `IosPlatformBridge.shared` API,
 * unlike top-level file functions whose generated facade class name depends on internal
 * file-name mangling - this object exists specifically to give Swift one dependable target.
 */
object IosPlatformBridge {

    /** Call once from `application(_:didFinishLaunchingWithOptions:)`, before it returns. */
    fun registerBackgroundTasks() {
        registerIosBackgroundTasks()
    }

    /** Set once at app launch to `{ WidgetCenter.shared.reloadAllTimelines() }`. */
    var widgetRefreshHook: (() -> Unit)?
        get() = iosWidgetRefreshHook
        set(value) {
            iosWidgetRefreshHook = value
        }
}
