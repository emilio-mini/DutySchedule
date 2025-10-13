package me.emiliomini.dutyschedule.shared.api

class IosRedirectApi : PlatformRedirectApi {

    override fun dialPhone(phone: String) {
        // TODO: Implement
    }

    override fun sendSms(phone: String) {
        // TODO: Implement
    }

    override fun sendWA(phone: String) {
        // TODO: Implement
    }

    override fun sendEmail(email: String) {
        // TODO: Implement
    }

}

actual fun initializePlatformRedirectApi(): PlatformRedirectApi {
    return IosRedirectApi()
}
