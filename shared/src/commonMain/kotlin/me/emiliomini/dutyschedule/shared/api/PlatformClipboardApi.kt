package me.emiliomini.dutyschedule.shared.api

interface PlatformClipboardApi {
    suspend fun copyToClipboard(text: String, label: String? = null)
}

expect fun initializePlatformClipboardApi(): PlatformClipboardApi

private var api: PlatformClipboardApi? = null

fun getPlatformClipboardApi(): PlatformClipboardApi = if (api == null) {
    api = initializePlatformClipboardApi()
    api!!
} else {
    api!!
}
