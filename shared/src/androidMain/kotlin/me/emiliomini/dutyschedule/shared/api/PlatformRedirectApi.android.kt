package me.emiliomini.dutyschedule.shared.api

import android.content.Intent
import androidx.core.net.toUri

class AndroidRedirectApi : PlatformRedirectApi {
    override fun dialPhone(phone: String) {
        val uri = "tel:${phone}".toUri()
        val intent = Intent(Intent.ACTION_DIAL, uri)
        APPLICATION_CONTEXT.startActivity(intent)
    }

    override fun sendSms(phone: String) {
        val uri = "smsto:${phone}".toUri()
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        if (intent.resolveActivity(APPLICATION_CONTEXT.packageManager) != null) {
            APPLICATION_CONTEXT.startActivity(intent)
        }
    }

    override fun sendWA(phone: String) {
        val fullNumber = phone.filter { it.isDigit() }
        val uri = "https://wa.me/$fullNumber/".toUri()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            setPackage("com.whatsapp")
        }

        if (intent.resolveActivity(APPLICATION_CONTEXT.packageManager) != null) {
            APPLICATION_CONTEXT.startActivity(intent)
        }
    }

    override fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(
                Intent.EXTRA_EMAIL, arrayOf(email)
            )
        }
        if (intent.resolveActivity(APPLICATION_CONTEXT.packageManager) != null) {
            APPLICATION_CONTEXT.startActivity(intent)
        }
    }

}

actual fun initializePlatformRedirectApi(): PlatformRedirectApi {
    return AndroidRedirectApi()
}