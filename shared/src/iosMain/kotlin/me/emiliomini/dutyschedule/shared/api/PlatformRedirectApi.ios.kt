package me.emiliomini.dutyschedule.shared.api

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosRedirectApi : PlatformRedirectApi {

    override fun dialPhone(phone: String) {
        openUrl("tel://$phone")
    }

    override fun sendSms(phone: String) {
        openUrl("sms://$phone")
    }

    override fun sendWA(phone: String) {
        val whatsappUrlString = "whatsapp://send?phone=$phone"
        val whatsappUrl = NSURL(string = whatsappUrlString)

        if (UIApplication.sharedApplication.canOpenURL(whatsappUrl)) {
            UIApplication.sharedApplication.openURL(whatsappUrl)
        } else {
            val appStoreUrl = NSURL(string = "https://itunes.apple.com/app/id310633997")
            UIApplication.sharedApplication.openURL(appStoreUrl)
        }
    }

    override fun sendEmail(email: String) {
        openUrl("mailto:$email")
    }

    private fun openUrl(urlString: String) {
        val url = NSURL(string = urlString)
        if (UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url)
        } else {
            println("Cannot open URL: $urlString")
        }
    }

}

actual fun initializePlatformRedirectApi(): PlatformRedirectApi {
    return IosRedirectApi()
}
