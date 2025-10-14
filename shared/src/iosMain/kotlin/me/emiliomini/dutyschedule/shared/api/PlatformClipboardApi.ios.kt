package me.emiliomini.dutyschedule.shared.api

import platform.UIKit.UIPasteboard

class IosClipboardApi : PlatformClipboardApi {

    override suspend fun copyToClipboard(text: String, label: String?) {
        UIPasteboard.generalPasteboard.string = text
    }

}

actual fun initializePlatformClipboardApi(): PlatformClipboardApi {
    return IosClipboardApi()
}
