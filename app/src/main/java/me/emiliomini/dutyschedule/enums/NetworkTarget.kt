package me.emiliomini.dutyschedule.enums

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

enum class NetworkTarget(val url: String) {
    LATEST_RELEASE("https://api.github.com/repos/emilio-mini/DutySchedule/releases?per_page=1"),
    LOGIN("https://dienstplan.o.roteskreuz.at/login.php"),
    KEEP_ALIVE("https://dienstplan.o.roteskreuz.at/keepAlive.php"),
    LOAD_PLAN("https://dienstplan.o.roteskreuz.at/StaffPortal/plan/data/loadPlan.json"),
    GET_SHIFTS("https://dienstplan.o.roteskreuz.at/StaffPortal/duties/data/getShifts.json"),
    GET_STAFF("https://dienstplan.o.roteskreuz.at/StaffPortal/staff/data/getStaff.json");

    fun httpUrl(): HttpUrl {
        return url.toHttpUrl()
    }
}