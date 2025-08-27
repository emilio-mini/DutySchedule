package me.emiliomini.dutyschedule.enums

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

enum class NetworkTarget(val url: String) {
    LATEST_RELEASE("https://api.github.com/repos/emilio-mini/DutySchedule/releases?per_page=1"),
    SCHEDULE_BASE("https://dienstplan.o.roteskreuz.at"),

    // Used for login requests
    LOGIN(SCHEDULE_BASE.url + "/login.php"),

    // Used to keep authentication alive (5min timer)
    KEEP_ALIVE(SCHEDULE_BASE.url + "/keepAlive.php"),

    // Contains some useful information about the user
    DISPO(SCHEDULE_BASE.url + "/StaffPortal/dispo.php"),

    // Used to load the whole plan for a station within a timeframe
    LOAD_PLAN(SCHEDULE_BASE.url + "/StaffPortal/plan/data/loadPlan.json"),

    // Used to load upcoming duties for the logged in user
    LOAD_UPCOMING(SCHEDULE_BASE.url + "/StaffPortal/duties/data/load.json"),

    // Used to load past duties for the logged in user
    LOAD_PAST(SCHEDULE_BASE.url + "/StaffPortal/archive/data/loadDuties.json"),

    // Used to get a list of messages for resources within a timeframe
    GET_MESSAGES(SCHEDULE_BASE.url + "/Ressourcen/messages/data/getMessages.json"),

    // Used to get a list of resources (mostly useless but provides the only way to link messages to vehicles)
    GET_RESOURCES(SCHEDULE_BASE.url + "/StaffPortal/ressources/data/getRessources.json"),

    // Returns a list of all possible shift timings
    GET_SHIFTS(SCHEDULE_BASE.url + "/StaffPortal/duties/data/getShifts.json"),

    // Used to retrieve details about staff members
    GET_STAFF(SCHEDULE_BASE.url + "/StaffPortal/staff/data/getStaff.json");

    fun httpUrl(): HttpUrl {
        return url.toHttpUrl()
    }

    companion object {

        fun withScheduleBase(url: String): String {
            return SCHEDULE_BASE.url + url
        }

    }
}