package me.emiliomini.dutyschedule.shared.services.network

/**
 * Paths below the configured PREP base. [url] resolves on every access rather than being built
 * once, because the base is supplied by the user and can change while the app is running.
 */
enum class Endpoints(private val path: String) {

    // Used for login requests
    LOGIN("/login.php"),

    // Used to keep authentication alive (5min timer)
    KEEP_ALIVE("/keepAlive.php"),

    // Contains some useful information about the user
    DISPO("/StaffPortal/dispo.php"),

    // Used to load the whole plan for a station within a timeframe
    LOAD_PLAN("/StaffPortal/plan/data/loadPlan.json"),

    // Used to load upcoming duties for the logged in user
    LOAD_UPCOMING("/StaffPortal/duties/data/load.json"),

    // Used to load past duties for the logged in user
    LOAD_PAST("/StaffPortal/archive/data/loadDuties.json"),

    // Used to get a list of messages for resources within a timeframe
    GET_MESSAGES("/Ressourcen/messages/data/getMessages.json"),

    // Used to get a list of resources (mostly useless but provides the only way to link messages to vehicles)
    GET_RESOURCES("/StaffPortal/ressources/data/getRessources.json"),

    // Returns a list of all possible shift timings
    GET_SHIFTS("/StaffPortal/duties/data/getShifts.json"),

    // Used to retrieve details about staff members
    GET_STAFF("/StaffPortal/staff/data/getStaff.json"),

    // Used to allocate a duty slot
    CREATE_AND_ALLOCATE_DUTY("/StaffPortal/duties/data/createAndAllocateDuty.json");

    val url: String
        get() = withScheduleBase(path)

    companion object {
        private const val DOCSCED_PATH = "/index.php"

        val scheduleBase: String
            get() = EndpointService.prepUrl

        val docsced: String
            get() = EndpointService.docscedUrl + DOCSCED_PATH

        fun withScheduleBase(url: String): String {
            return scheduleBase + url
        }
    }
}
