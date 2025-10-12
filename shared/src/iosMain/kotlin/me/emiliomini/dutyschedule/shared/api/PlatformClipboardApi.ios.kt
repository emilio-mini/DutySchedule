package me.emiliomini.dutyschedule.shared.api

class IosClipboardApi : PlatformClipboardApi {

    override suspend fun copyToClipboard(text: String, label: String?) {
        // TODO: Implement
    }

}

actual fun initializePlatformClipboardApi(): PlatformClipboardApi {
    return IosClipboardApi()
}
