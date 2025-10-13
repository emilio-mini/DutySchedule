package me.emiliomini.dutyschedule.shared.api

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

lateinit var LOCAL_CLIPBOARD: Clipboard

class AndroidClipboardApi : PlatformClipboardApi {
    override suspend fun copyToClipboard(text: String, label: String?) {
        LOCAL_CLIPBOARD.setClipEntry(
            ClipEntry(
                ClipData.newPlainText(
                    label ?: "",
                    text
                )
            )
        )
    }
}

actual fun initializePlatformClipboardApi(): PlatformClipboardApi {
    return AndroidClipboardApi()
}