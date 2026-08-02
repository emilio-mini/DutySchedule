@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.services.prep.live

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import me.emiliomini.dutyschedule.shared.api.getPlatformConnectivityApi
import me.emiliomini.dutyschedule.shared.api.getPlatformLogger
import me.emiliomini.dutyschedule.shared.comparators.DutyDefinitionComparator
import me.emiliomini.dutyschedule.shared.datastores.CreateDutyResponse
import me.emiliomini.dutyschedule.shared.datastores.DutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.DutyGroup
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.Incode
import me.emiliomini.dutyschedule.shared.datastores.Message
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Org
import me.emiliomini.dutyschedule.shared.datastores.OrgDay
import me.emiliomini.dutyschedule.shared.datastores.OrgItems
import me.emiliomini.dutyschedule.shared.datastores.Requirement
import me.emiliomini.dutyschedule.shared.datastores.Slot
import me.emiliomini.dutyschedule.shared.datastores.YearlyDutyItems
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import me.emiliomini.dutyschedule.shared.mappings.docScedConfigFromString
import me.emiliomini.dutyschedule.shared.services.AlarmService.updateAlarms
import me.emiliomini.dutyschedule.shared.services.CredentialService
import me.emiliomini.dutyschedule.shared.services.network.Endpoints
import me.emiliomini.dutyschedule.shared.services.network.MultiplatformNetworkAdapter
import me.emiliomini.dutyschedule.shared.services.network.NetworkService
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleServiceBase
import me.emiliomini.dutyschedule.shared.services.prep.parsing.DataExtractorService
import me.emiliomini.dutyschedule.shared.services.prep.parsing.DataParserService
import me.emiliomini.dutyschedule.shared.services.prep.parsing.DocScedParserService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.getAllVehicles
import me.emiliomini.dutyschedule.shared.util.isInvalid
import me.emiliomini.dutyschedule.shared.util.isNight
import me.emiliomini.dutyschedule.shared.util.isNightShift
import me.emiliomini.dutyschedule.shared.util.isNotNullOrBlank
import me.emiliomini.dutyschedule.shared.util.midpointInstant
import me.emiliomini.dutyschedule.shared.util.nullIfBlank
import me.emiliomini.dutyschedule.shared.util.toInstant
import me.emiliomini.dutyschedule.shared.util.toTimestamp
import me.emiliomini.dutyschedule.shared.util.withinLast
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object PrepService : DutyScheduleServiceBase {
    private val CONNECTIVITY_SETTLE = 2.seconds

    /**
     * How long a stored employee stays usable before it is pulled again. Qualifications and contact
     * details change rarely, so this refresh runs in the background behind the already drawn roster
     */
    private val EMPLOYEE_MAX_AGE = 7.days

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val logger = getPlatformLogger("PrepService")
    private var incode: Incode? = null
    private val messages = mutableMapOf<String, List<Message>>()

    private val employeeRefreshMutex = Mutex()
    private val refreshingEmployees = mutableSetOf<String>()

    private var isRestoringLogin by mutableStateOf(false)
    override var isLoggedIn by mutableStateOf(false)
    override var self by mutableStateOf<Employee?>(null)

    init {
        scope.launch {
            var wasConnected = false
            var pendingRestore: Job? = null

            getPlatformConnectivityApi().isConnected.collect { connected ->
                pendingRestore?.cancel()

                if (!connected) {
                    wasConnected = false
                    return@collect
                }

                pendingRestore = scope.launch {
                    delay(CONNECTIVITY_SETTLE)
                    if (!wasConnected) {
                        wasConnected = true
                        restoreLogin()
                    }
                }
            }
        }
    }

    override fun getMessages(): Map<String, List<Message>> {
        return this.messages
    }

    override fun getIncode(): Incode? {
        return this.incode
    }

    override suspend fun getOrg(abbreviationOrIdentifier: String): Org? {
        val orgs = this.loadOrgs()?.orgs?.values ?: return null

        var org = orgs.firstOrNull { it.abbreviation == abbreviationOrIdentifier }
        if (org == null) {
            org = orgs.firstOrNull { it.identifier == abbreviationOrIdentifier }
        }

        return org
    }

    override suspend fun login(username: String, password: String): Boolean {
        logger.d("Running login...")
        MultiplatformNetworkAdapter.clearCookies()
        val loginResult = NetworkService.login(username, password) ?: return false

        logger.d("Resulted in status ${loginResult.status}")
        val responseBody = loginResult.bodyAsText()
        if (responseBody.isBlank()) {
            logger.w("Login response empty. Cancelling...")
            return false
        }

        val incode = DataExtractorService.extractIncode(responseBody)
        logger.d("Extracted incode ${incode?.token}:${incode?.value?.dropLast(4)}**** from response")

        if (incode != null) {
            this.incode = incode

            StorageService.USER_PREFERENCES.update {
                it.copy(username = username)
            }
            CredentialService.setPassword(password)

            StorageService.INCODE.update {
                this.incode ?: it
            }

            logger.d("Logged in!")
        } else {
            logger.w("Could not find x-incode in response body")
            return false
        }
        this.isLoggedIn = true

        var orgs: OrgItems?
        var allowedOrgs: List<String>? = null
        try {
            orgs = this.loadOrgs()
            if (orgs != null) {
                logger.d("Loaded ${orgs.orgs.size} orgs")
            } else {
                logger.w("Could not load orgs")
            }

            allowedOrgs = this.loadAllowedOrgs()
            if (allowedOrgs != null) {
                logger.d("Loaded ${allowedOrgs.size} allowed orgs")
            } else {
                logger.w("Could not load allowed orgs")
            }
        } catch (e: Error) {
            logger.e("${e.message}")
        }

        val guid = DataExtractorService.extractGUID(responseBody)
        logger.d("Extracted self guid: $guid")
        if (guid != null && allowedOrgs != null) {
            this.self = loadSelf(guid, allowedOrgs.first())
            logger.d("Loaded self identity")
        } else {
            logger.w("Could not find self dataGuid in response body")
        }

        return true
    }

    override suspend fun previouslyLoggedIn(): Boolean {
        val username = StorageService.USER_PREFERENCES.get()?.username ?: return false

        return username.isNotBlank() && CredentialService.getPassword().isNotNullOrBlank()
    }

    override suspend fun restoreLogin(): Boolean {
        if (isRestoringLogin) {
            return false
        }
        isRestoringLogin = true

        try {
            logger.d("Trying to restore login...")
            val localPreferences = StorageService.USER_PREFERENCES.get()
            val username = localPreferences?.username
            val password = CredentialService.getPassword()

            if (username.isNullOrBlank() || password.isNullOrBlank()) {
                logger.d("No username or password found")
                return false
            }

            this.self = this.loadSelf(null, null)
            if (this.self == null) {
                return this.login(username, password)
            }

            val result = NetworkService.getBase()
            val resultBody = result?.bodyAsText()
            if (!resultBody.isNullOrEmpty()) {
                val incode = DataExtractorService.extractIncode(resultBody)

                if (incode != null) {
                    this.incode = incode

                    StorageService.INCODE.update {
                        this.incode ?: it
                    }
                    this.isLoggedIn = true

                    logger.d("Restored login from previous session")
                    return true
                }
            }

            logger.d("Restoring by re-running login process")
            return this.login(username, password)
        } finally {
            isRestoringLogin = false
        }
    }

    override suspend fun logout() {
        this.isLoggedIn = false
        this.incode = null
        this.self = null
        this.messages.clear()
        CredentialService.clearPassword()
        StorageService.clear()
        MultiplatformNetworkAdapter.clearCookies()
    }

    override suspend fun loadSelf(guid: String?, org: String?): Employee? {
        val localSelf = StorageService.SELF.get()
        if (localSelf != null && localSelf.guid.isNotBlank()) {
            this.self = localSelf
            return localSelf
        }

        if (guid == null || org == null) {
            return null
        }

        val staffResult = this.getStaff(
            org,
            listOf(guid),
            Clock.System.now(),
            Clock.System.now()
        )
        if (staffResult.isNotEmpty()) {
            this.self = staffResult.firstOrNull { it.guid == guid }
            if (this.self != null) {
                StorageService.SELF.update {
                    this.self!!
                }
            }
            logger.d("Loaded identity")
            return this.self
        }

        logger.d("Failed to load identity")
        return null
    }

    override suspend fun loadOrgs(): OrgItems? {
        val orgs = StorageService.ORG_ITEMS.get()
        if (orgs == null || orgs.orgs.isEmpty()) {
            val dispoBody = NetworkService.dispo()?.bodyAsText()
            if (dispoBody.isNullOrBlank()) {
                logger.e("Failed to load orgs - missing dispo body or network failure")
                return null
            }

            val orgTreeLocation = DataExtractorService.extractOrgTreeUrl(dispoBody)
            if (orgTreeLocation == null) {
                logger.e("Failed to load orgs - missing org tree")
                return null
            }

            val orgTreeUrl = Endpoints.withScheduleBase(orgTreeLocation)
            val orgTreeBody = MultiplatformNetworkAdapter.get(orgTreeUrl)?.bodyAsText() ?: ""
            if (orgTreeBody.isBlank()) {
                logger.e("Failed to load orgs - missing org tree body")
                return null
            }

            val jsonStart = orgTreeBody.indexOf('{')
            val jsonEnd = orgTreeBody.lastIndexOf('}')
            val orgTreeJson = orgTreeBody.substring(jsonStart, jsonEnd + 1)
            val orgItems = DataParserService.parseOrgTree(Json.parseToJsonElement(orgTreeJson))
            if (orgItems == null) {
                logger.e("Failed to load orgs - invalid org tree")
                return null
            }

            StorageService.ORG_ITEMS.update { orgItems }

            return orgItems
        }

        return orgs
    }

    override suspend fun loadAllowedOrgs(): List<String>? {
        val dispoBody = NetworkService.dispo()?.bodyAsText()
        if (dispoBody.isNullOrEmpty()) {
            logger.e("Failed to load allowed orgs - missing dispo body or network failure")
            return null
        }

        val allowedOrgs = DataExtractorService.extractAllowedOrgs(dispoBody) ?: return null
        StorageService.USER_PREFERENCES.update {
            it.copy(allowedOrgs = allowedOrgs)
        }

        return allowedOrgs
    }

    override suspend fun loadPlan(
        orgUnitDataGuid: String,
        from: Instant,
        to: Instant
    ): Pair<List<DutyDefinition>, Map<String, DutyGroup>>? {
        logger.d("Loading plan...")

        val code = this.incode
        if (!this.isLoggedIn || code == null || code.isInvalid()) {
            logger.w("loadPlan: no usable session")
            return null
        }

        val planBody = NetworkService.loadPlan(code, orgUnitDataGuid, from, to)?.bodyAsText()
        if (planBody.isNullOrBlank()) {
            logger.w("loadPlan: empty response")
            return null
        }

        try {
            return DataParserService.parseLoadPlan(Json.parseToJsonElement(planBody))
        } catch (e: IllegalArgumentException) {
            logger.e("Invalid JSON! $planBody", e)
            return null
        }
    }

    override suspend fun getStaff(
        orgUnitDataGuid: String,
        staffDataGuid: List<String>,
        from: Instant,
        to: Instant
    ): List<Employee> {
        logger.d("Getting staff...")

        val code = this.incode
        if (!this.isLoggedIn || code == null || code.isInvalid()) {
            return emptyList()
        }

        val staffBody = NetworkService.getStaff(code, orgUnitDataGuid, staffDataGuid, from, to)
            ?.bodyAsText()
        if (staffBody.isNullOrBlank()) {
            return emptyList()
        }

        try {
            val refreshedAt = Clock.System.now().toTimestamp()
            val staff = DataParserService.parseGetStaff(Json.parseToJsonElement(staffBody))
                .map { it.copy(refreshedAt = refreshedAt) }

            StorageService.EMPLOYEES.update { items ->
                items.copy(
                    employees = items.employees + staff.associateBy { it.guid }
                )
            }
            return staff
        } catch (e: IllegalArgumentException) {
            logger.e("Invalid JSON! $staffBody", e)
            return emptyList()
        }
    }

    override suspend fun loadTimeline(
        orgUnitDataGuid: String,
        from: Instant,
        to: Instant
    ): List<OrgDay>? {
        // Load plan
        val plan = this.loadPlan(orgUnitDataGuid, from, to)
        if (plan == null) {
            logger.e("Failed to load plan")
            return null
        }

        var (duties, groups) = plan

        // Ensure staff is loaded
        val employeeGuids = duties.flatMap {
            it.slots.map {
                if (it.employeeGuid.isNullOrBlank()) null else it.employeeGuid
            }
        }.filterNotNull().distinct()
        val localEmployees = StorageService.EMPLOYEES.getOrDefault()
        val missingEmployeeGuids = employeeGuids.filter {
            !localEmployees.employees.containsKey(it)
        }
        val staleEmployeeGuids = employeeGuids.filter { guid ->
            val stored = localEmployees.employees[guid] ?: return@filter false
            !stored.refreshedAt?.toInstant().withinLast(EMPLOYEE_MAX_AGE)
        }
        logger.d("Missing employee guids: ${missingEmployeeGuids.joinToString(";")}")
        logger.d("Stale employee guids: ${staleEmployeeGuids.joinToString(";")}")

        if (missingEmployeeGuids.isNotEmpty()) {
            val staff = getStaff(orgUnitDataGuid, missingEmployeeGuids, from, to)
            if (staff.isEmpty()) {
                logger.e("Failed to load missing staff $missingEmployeeGuids")
            } else {
                logger.d("Loaded ${staff.size} staff elements")
            }
        }
        refreshEmployees(orgUnitDataGuid, staleEmployeeGuids, from, to)

        try {
            duties = augmentHaendWithDocScedTf(
                duties = duties,
                selectedOrgGuid = orgUnitDataGuid,
                from = from,
                to = to
            )
        } catch (e: Exception) {
            logger.w("HÄND-Augmentierung fehlgeschlagen: ${e.message}")
        }

        val days = mutableMapOf<String, OrgDay>()
        for (duty in duties) {
            val date = duty.begin.format("yyyy-MM-dd")
            val day = days.getOrElse(date) {
                OrgDay(orgUnitDataGuid, duty.begin)
            }

            if (duty.isNightShift()) {
                days[date] = day.copy(
                    groups = listOf(*day.groups.toTypedArray(), *groups.values.toTypedArray()),
                    nightShifts = listOf(*day.nightShifts.toTypedArray(), duty)
                )
            } else {
                days[date] = day.copy(
                    groups = listOf(*day.groups.toTypedArray(), *groups.values.toTypedArray()),
                    dayShifts = listOf(*day.dayShifts.toTypedArray(), duty)
                )
            }
        }

        var daysList = days.values.toList()
        daysList = daysList.map {
            val dayShifts = it.dayShifts.sortedWith(DutyDefinitionComparator)
            val nightShifts = it.nightShifts.sortedWith(DutyDefinitionComparator)

            it.copy(
                dayShifts = dayShifts,
                nightShifts = nightShifts
            )
        }

        logger.d("Loaded ${days.size} days on the timeline")
        return daysList
    }

    override suspend fun loadPast(year: String): List<MinimalDutyDefinition>? {
        val intYear = year.toIntOrNull()
        if (intYear == null) {
            logger.e("loadPast: '$year' is not a year")
            return null
        }

        val cached = StorageService.PAST_DUTIES.get()?.years?.get(intYear)?.minimalDutyDefinitions
        if (cached != null && !isLoggedIn) {
            return cached
        }

        val code = this.incode
        if (code == null || code.isInvalid()) {
            logger.w("loadPast: no usable session, keeping cached data")
            return cached
        }

        val pastResponse = NetworkService.loadPast(code, year)?.bodyAsText()
        if (pastResponse.isNullOrBlank()) {
            logger.w("loadPast: empty response, keeping cached data")
            return null
        }

        val pastDuties = parseMinimalDuties(pastResponse)
        if (pastDuties == null) {
            return null
        }

        StorageService.PAST_DUTIES.update {
            it.copy(
                years = it.years + (intYear to YearlyDutyItems(pastDuties, intYear))
            )
        }
        return pastDuties
    }

    override suspend fun loadHoursOfService(year: String): Float? {
        val localStats = StorageService.STATISTICS.get()
        if (localStats != null && !isLoggedIn) {
            return localStats.minutesServed / 60f
        }

        val yearData = loadPast(year)
        if (yearData == null) {
            logger.w("loadHoursOfService: load failed, keeping the stored quota")
            return null
        }

        val minutesServed = yearData.sumOf { it.duration }
        StorageService.STATISTICS.update {
            it.copy(
                minutesServed = minutesServed
            )
        }

        return minutesServed / 60f
    }

    override suspend fun loadUpcoming(): List<MinimalDutyDefinition>? {
        val localUpcoming = StorageService.UPCOMING_DUTIES.get()?.minimalDutyDefinitions
        if (localUpcoming != null && !isLoggedIn) {
            return localUpcoming
        }

        val code = this.incode
        if (code == null || code.isInvalid()) {
            logger.w("loadUpcoming: no usable session, keeping cached data")
            return localUpcoming
        }

        val upcomingResponse = NetworkService.loadUpcoming(code)?.bodyAsText()
        if (upcomingResponse.isNullOrBlank()) {
            logger.w("loadUpcoming: empty response, keeping cached data")
            return null
        }

        val upcomingDuties = parseMinimalDuties(upcomingResponse)
        if (upcomingDuties == null) {
            return null
        }

        if (localUpcoming != null) {
            updateAlarms(localUpcoming, upcomingDuties)
        }

        StorageService.UPCOMING_DUTIES.update {
            it.copy(
                minimalDutyDefinitions = upcomingDuties
            )
        }
        refreshSelfIfStale()

        return upcomingDuties
    }

    override suspend fun loadMessages(
        orgUnitDataGuid: String,
        from: Instant,
        to: Instant
    ): List<Message> {
        val code = this.incode
        if (!isLoggedIn || code == null || code.isInvalid()) {
            return emptyList()
        }

        val messagesResponse =
            NetworkService.getMessages(code, orgUnitDataGuid, from, to)?.bodyAsText()
        if (messagesResponse.isNullOrBlank()) {
            return emptyList()
        }

        val messages = try {
            DataParserService.parseGetMessages(Json.parseToJsonElement(messagesResponse))
        } catch (e: IllegalArgumentException) {
            logger.e("Invalid JSON! $messagesResponse", e)
            null
        }
        if (messages == null) {
            return emptyList()
        }

        for (message in messages) {
            val messageList = this.messages.getOrElse(message.resourceGuid) { emptyList() }
                .toMutableList()
            if (messageList.find { it.guid == message.guid } == null) {
                messageList.add(message)
            }
            this.messages[message.resourceGuid] = messageList
        }

        logger.d("Loaded ${messages.size} messages")
        return messages
    }

    override suspend fun createAndAllocateDuty(planDataGuid: String): CreateDutyResponse? {
        val code = getIncode() ?: return null

        val body =
            NetworkService.createAndAllocateDuty(code, planDataGuid)?.bodyAsText().nullIfBlank()
                ?: return null

        val parsed = try {
            DataParserService.parseCreateAndAllocateDuty(Json.parseToJsonElement(body))
        } catch (e: Exception) {
            logger.e("Invalid JSON! $body $e")
            null
        }

        return parsed
    }

    /**
     * Pulls employees again without blocking the caller. Guids already being refreshed are skipped,
     * so paging back and forth through the schedule cannot pile up requests for the same people
     */
    private fun refreshEmployees(
        orgUnitDataGuid: String,
        guids: List<String>,
        from: Instant,
        to: Instant
    ) {
        if (guids.isEmpty()) {
            return
        }

        scope.launch {
            val pending = employeeRefreshMutex.withLock {
                val new = guids.filterNot { refreshingEmployees.contains(it) }
                refreshingEmployees.addAll(new)
                new
            }
            if (pending.isEmpty()) {
                return@launch
            }

            try {
                logger.d("Refreshing ${pending.size} stale employees")
                getStaff(orgUnitDataGuid, pending, from, to)
            } finally {
                employeeRefreshMutex.withLock { refreshingEmployees.removeAll(pending.toSet()) }
            }
        }
    }

    /**
     * Keeps the stored identity current. Own qualifications gate which slots may be self assigned,
     * so a record frozen at first login keeps handing out the wrong answer
     */
    private fun refreshSelfIfStale() {
        val current = this.self ?: return
        if (current.refreshedAt?.toInstant().withinLast(EMPLOYEE_MAX_AGE)) {
            return
        }

        scope.launch {
            val org = StorageService.USER_PREFERENCES.get()?.allowedOrgs?.firstOrNull()
                ?: current.defaultOrg
                ?: return@launch

            val now = Clock.System.now()
            val refreshed = getStaff(org, listOf(current.guid), now, now)
                .firstOrNull { it.guid == current.guid } ?: return@launch

            self = refreshed
            StorageService.SELF.update { refreshed }
            logger.d("Refreshed own identity")
        }
    }

    private fun parseMinimalDuties(body: String): List<MinimalDutyDefinition>? {
        val json = try {
            Json.parseToJsonElement(body)
        } catch (e: IllegalArgumentException) {
            logger.e("Invalid JSON! $body", e)
            return null
        }

        return DataParserService.parseLoadMinimalDutyDefinitions(json)
    }

    private suspend fun augmentHaendWithDocScedTf(
        duties: List<DutyDefinition>,
        selectedOrgGuid: String,
        from: Instant,
        to: Instant
    ): List<DutyDefinition> {
        val config = docScedConfigFromString(selectedOrgGuid)
        if (config == null) {
            logger.d("DocSced: org is not a valid HAEND org – skip")
            return duties
        }

        val html = NetworkService.loadDocScedCalendar(config, gran = "ges")?.bodyAsText()
        if (html.isNullOrEmpty()) {
            logger.w("DocSced: failed to load html for org: $config; Possible network failure")
            return duties
        }
        val days = DocScedParserService.parseHaendData(html).getOrNull().orEmpty()
        val byDate = days.associateBy { it.date }
        logger.d("DocSced: ${days.size} Fahrdienst-Tage geparst für $config")

        val mutableDuties = duties.toMutableList()
        for (i in mutableDuties.indices) {
            val duty = mutableDuties[i]
            val haends = duty.getAllVehicles(listOf(RequirementMapping.HAEND.value))
            if (haends.isEmpty()) continue

            for (haend in haends) {
                val mid = midpointInstant(
                    haend.begin.toInstant(),
                    haend.end.toInstant()
                )
                val night = mid.isNight()
                val dsDate = docscedRowDate(mid).toLocalDateTime(TimeZone.currentSystemDefault()).date

                val dsDay = byDate[dsDate]
                if (dsDay == null) {
                    logger.d("DocSced: kein Zeilendatum $dsDate ($config)")
                    continue
                }

                logger.d("DocSced[$config][$dsDate] TAG=${dsDay.day.joinToString()} | NACHT=${dsDay.night.joinToString()}")

                val candidate = if (night) dsDay.night.firstOrNull() else dsDay.day.firstOrNull()
                if (candidate.isNullOrBlank()) {
                    logger.d("DocSced: kein ${if (night) "Nacht" else "Tag"}-Arzt am $dsDate ($config)")
                    continue
                }

                val docEmployee = Employee(
                    guid = "docsced:$config:$dsDate:${if (night) "night" else "day"}",
                    name = candidate,
                    identifier = "Dr."
                )

                mutableDuties[i] = duty.copy(
                    slots = duty.slots.toMutableList().let {
                        it.add(
                            min(2, it.size - 1),
                            Slot(
                                begin = haend.begin,
                                end = haend.end,
                                employeeGuid = "doc",
                                requirement = Requirement(RequirementMapping.HAEND_DR.value),
                                inlineEmployee = docEmployee
                            )
                        )
                        it
                    }
                )

                logger.d(
                    "DocSced: ${docEmployee.name} → TF gesetzt ($config, $dsDate, ${if (night) "Nacht" else "Tag"}) " +
                            "für HÄND ${
                                haend.begin.format("yyyy-MM-dd HH:mm")
                            }–${haend.end.format("yyyy-MM-dd HH:mm")}"
                )

            }
        }

        return mutableDuties.toList()
    }

    /**
     * Returns the previous day if the midpoint lies between midnight and 6 AM as nightshifts
     * are displayed on the day they begin
     */
    private fun docscedRowDate(
        mid: Instant,
        zone: TimeZone = TimeZone.currentSystemDefault()
    ): Instant {
        val t = mid.toLocalDateTime(zone)
        return if (t.hour < 6) mid.minus(24, DateTimeUnit.HOUR) else mid
    }
}