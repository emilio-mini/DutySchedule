package me.emiliomini.dutyschedule.services.network

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import me.emiliomini.dutyschedule.datastore.prep.IncodeProto
import me.emiliomini.dutyschedule.datastore.prep.duty.MinimalDutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.debug.DebugFlags.AVOID_PREP_API
import me.emiliomini.dutyschedule.enums.NetworkTarget
import me.emiliomini.dutyschedule.models.network.CreateDutyResponse
import me.emiliomini.dutyschedule.models.prep.AssignedEmployee
import me.emiliomini.dutyschedule.models.prep.DutyDefinition
import me.emiliomini.dutyschedule.models.prep.Employee
import me.emiliomini.dutyschedule.models.prep.Incode
import me.emiliomini.dutyschedule.models.prep.Message
import me.emiliomini.dutyschedule.models.prep.MinimalDutyDefinition
import me.emiliomini.dutyschedule.models.prep.Org
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.services.parsers.DocScedParserService
import me.emiliomini.dutyschedule.models.prep.OrgDay
import me.emiliomini.dutyschedule.services.storage.DataKeys
import me.emiliomini.dutyschedule.services.storage.DataStores
import me.emiliomini.dutyschedule.services.storage.StorageService
import me.emiliomini.dutyschedule.util.toOffsetDateTime
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.ZonedDateTime

object PrepService {
    private const val TAG = "PrepService"

    private var incode: IncodeProto? = null

    private val messages = mutableMapOf<String, List<Message>>()

    private val dutyComparator = Comparator<DutyDefinition> { d1, d2 ->
        fun DutyDefinition.nonEmptySecondaryCount(): Int =
            listOf(el, tf, rs).count { it.isNotEmpty() }

        val d1HasSew = d1.sew.isNotEmpty()
        val d2HasSew = d2.sew.isNotEmpty()

        if (d1HasSew && !d2HasSew) return@Comparator -1
        if (!d1HasSew && d2HasSew) return@Comparator 1

        if (d1HasSew && d2HasSew) {
            val name1 = d1.sew.first().employee.name
            val name2 = d2.sew.first().employee.name
            val nameCmp = name1.compareTo(name2)
            if (nameCmp != 0) return@Comparator nameCmp
        }

        val d1Count = d1.nonEmptySecondaryCount()
        val d2Count = d2.nonEmptySecondaryCount()
        return@Comparator d1Count.compareTo(d2Count)
    }

    var isLoggedIn by mutableStateOf(false);
    var self by mutableStateOf<EmployeeProto?>(null)

    fun getMessages(): Map<String, List<Message>> {
        return this.messages
    }

    fun getIncode(): IncodeProto? {
        return this.incode
    }

    suspend fun getOrg(abbreviationOrIdentifier: String): OrgProto? {
        val orgs = this.loadOrgs()?.orgsMap?.values ?: return null

        var org = orgs.firstOrNull { it.abbreviation == abbreviationOrIdentifier }
        if (org == null) {
            org = orgs.firstOrNull { it.identifier == abbreviationOrIdentifier }
        }

        return org
    }

    suspend fun login(username: String, password: String): Boolean {
        val loginResult: Result<String?> = if (AVOID_PREP_API.active()) {
            Result.success("...{ headers: { 'x-incode-EXAMPLEKEY': 'EXAMPLEVALUE' } }...")
        } else {
            NetworkService.login(username, password)
        }

        val responseBody = loginResult.getOrNull()
        if (responseBody.isNullOrBlank()) {
            return false
        }

        val incode = DataExtractorService.extractIncode(responseBody)

        if (incode != null) {
            this.incode = incode

            StorageService.save(DataKeys.USERNAME, username)
            StorageService.save(DataKeys.PASSWORD, password)
            DataStores.INCODE.updateData {
                this.incode!!
            }

            Log.d(TAG, "Logged in!")
        } else {
            Log.w(TAG, "Could not find x-incode in response body")
            return false
        }
        this.isLoggedIn = true

        var orgs: OrgItemsProto?
        var allowedOrgs: List<String>? = null
        try {
            orgs = this.loadOrgs()
            if (orgs != null) {
                Log.d(TAG, "Loaded ${orgs.orgsCount} orgs")
            } else {
                Log.w(TAG, "Could not load orgs")
            }

            allowedOrgs = this.loadAllowedOrgs()
            if (allowedOrgs != null) {
                Log.d(TAG, "Loaded ${allowedOrgs.size} allowed orgs")
            } else {
                Log.w(TAG, "Could not load allowed orgs")
            }
        } catch (e: Error) {
            Log.e(TAG, "${e.message}")
        }

        val guid = DataExtractorService.extractGUID(responseBody)
        Log.d(TAG, "Extracted self guid: $guid")
        if (guid != null && allowedOrgs != null) {
            this.self = loadSelf(guid, allowedOrgs.first())
            Log.d(TAG, "Loaded self identity")
        } else {
            Log.w(TAG, "Could not find self dataGuid in response body")
        }

        return true
    }

    suspend fun previouslyLoggedIn(): Boolean {
        val username = StorageService.load(DataKeys.USERNAME)
        val password = StorageService.load(DataKeys.PASSWORD)

        return username != null && password != null
    }

    suspend fun restoreLogin(): Boolean {
        val username = StorageService.load(DataKeys.USERNAME)
        val password = StorageService.load(DataKeys.PASSWORD)
        val incode = DataStores.INCODE.data.firstOrNull()
        val maxAge = 5L * 60L * 1_000L

        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            return false
        }

        if (AVOID_PREP_API.active()) {
            return this.login(username, password)
        }

        val lastIncodeUseMillis = incode?.lastUsed?.toOffsetDateTime()?.toInstant()?.toEpochMilli() ?: 0L
        if (incode != null && System.currentTimeMillis() - maxAge >= lastIncodeUseMillis) {
            val keepAlive = NetworkService.keepAlive().getOrNull()
            if (keepAlive == "true") {
                this.incode = incode
                this.isLoggedIn = true
                return true
            }
        }

        return this.login(username, password)
    }

    suspend fun logout() {
        this.incode = null
        StorageService.clear(DataKeys.USERNAME)
        StorageService.clear(DataKeys.PASSWORD)
    }

    suspend fun loadSelf(guid: String?, org: String?): EmployeeProto? {
        val localSelf = DataStores.SELF.data.firstOrNull()
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
            OffsetDateTime.now(),
            OffsetDateTime.now()
        ).getOrNull()
        if (staffResult != null && staffResult.isNotEmpty()) {
            this.self = staffResult.firstOrNull { it.guid == guid }?.toProto()
            if (this.self != null) {
                DataStores.SELF.updateData {
                    this.self!!
                }
            }
            return this.self
        }

        Log.d(TAG, "Loaded self identity")
        return null
    }

    suspend fun loadOrgs(): OrgItemsProto? {
        val orgs = DataStores.ORG_ITEMS.data.firstOrNull()
        if (orgs == null || orgs.orgsCount == 0) {
            val dispoBody = NetworkService.getDispo().getOrNull()
            if (dispoBody == null) {
                Log.e(TAG, "Failed to load orgs - missing dispo body")
                return null
            }

            val orgTreeLocation = DataExtractorService.extractOrgTreeUrl(dispoBody)
            if (orgTreeLocation == null) {
                Log.e(TAG, "Failed to load orgs - missing org tree")
                return null
            }

            val orgTreeUrl = NetworkTarget.withScheduleBase(orgTreeLocation).toHttpUrl()
            val orgTreeBody = NetworkService.get(orgTreeUrl).getOrNull()
            if (orgTreeBody == null) {
                Log.e(TAG, "Failed to load orgs - missing org tree body")
                return null
            }

            val jsonStart = orgTreeBody.indexOf('{')
            val jsonEnd = orgTreeBody.lastIndexOf('}')
            val orgTreeJson = orgTreeBody.substring(jsonStart, jsonEnd + 1)
            val orgItems = DataParserService.parseOrgTree(JSONObject(orgTreeJson))
            if (orgItems == null) {
                Log.e(TAG, "Failed to load orgs - invalid org tree")
                return null
            }

            DataStores.ORG_ITEMS.updateData { orgItems }

            return orgItems
        }

        return orgs
    }

    suspend fun loadAllowedOrgs(): List<String>? {
        val dispoBody = NetworkService.getDispo().getOrNull()
        if (dispoBody == null) {
            Log.e(TAG, "Failed to load allowed orgs - missing dispo body")
            return null
        }

        val allowedOrgs = DataExtractorService.extractAllowedOrgs(dispoBody) ?: return null
        StorageService.save(
            DataKeys.ALLOWED_ORGS,
            allowedOrgs.joinToString(StorageService.DEFAULT_SEPARATOR)
        )

        return allowedOrgs
    }

    suspend fun loadPlan(
        orgUnitDataGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<DutyDefinition>> {
        Log.d(TAG, "Loading plan...")

        if (!this.isLoggedIn) {
            return Result.failure(IOException("Not logged in!"))
        }

        if (AVOID_PREP_API.active()) {
            delay(2000)
            return Result.success(
                listOf(
                    DutyDefinition.getSample()
                )
            )
        }

        val planBody = NetworkService.loadPlan(incode!!, orgUnitDataGuid, from, to).getOrNull()
        if (planBody.isNullOrBlank()) {
            return Result.failure(IOException("Failed to load plan!"))
        }

        try {
            val duties = DataParserService.parseLoadPlan(JSONObject(planBody))
            return Result.success(duties)
        } catch (e: JSONException) {
            Log.e(TAG, "Invalid JSON! $planBody")
            return Result.failure(e)
        }
    }

    suspend fun getStaff(
        orgUnitDataGuid: String,
        staffDataGuid: List<String>,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<Employee>> {
        Log.d(TAG, "Getting staff...")

        if (!this.isLoggedIn) {
            return Result.failure(IOException("Not logged in!"))
        }

        if (AVOID_PREP_API.active()) {
            return Result.success(
                listOf(
                    Employee("e0", "Your Name", "00001234"),
                    Employee("e1", "John Doe", "00012345"),
                    Employee("e2", "Jane Doe", "00023456")
                )
            )
        }

        val staffBody =
            NetworkService.getStaff(incode!!, orgUnitDataGuid, staffDataGuid, from, to).getOrNull()
        if (staffBody.isNullOrBlank()) {
            return Result.failure(IOException("Failed to get staff!"))
        }

        try {
            val staff = DataParserService.parseGetStaff(JSONObject(staffBody))
            return Result.success(staff)
        } catch (e: JSONException) {
            Log.e(TAG, "Invalid JSON! $staffBody")
            return Result.failure(e)
        }
    }

    suspend fun loadTimeline(
        orgUnitDataGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<OrgDay>> {
        val plan = this.loadPlan(orgUnitDataGuid, from, to).getOrNull()
        if (plan.isNullOrEmpty()) {
            Log.e(TAG, "Failed to load plan")
            return Result.failure(IOException("Failed to load plan!"))
        }

        val employeeGuids = plan.flatMap { duty ->
            val guids = mutableListOf<String>()
            guids.addAll(duty.el.map { it.employee.guid })
            guids.addAll(duty.tf.map { it.employee.guid })
            guids.addAll(duty.rs.map { it.employee.guid })
            guids
        }.distinct()
        val staff = this.getStaff(orgUnitDataGuid, employeeGuids, from, to).getOrNull()
        if (staff.isNullOrEmpty()) {
            Log.e(TAG, "Failed to get staff")
            return Result.failure(IOException("Failed to get staff!"))
        }
        val staffMap = staff.associateBy { it.guid }

        plan.forEach { dutyDefinition ->
            dutyDefinition.el.forEach { assigned ->
                assigned.employee = staffMap[assigned.employee.guid] ?: assigned.employee
            }
            dutyDefinition.tf.forEach { assigned ->
                assigned.employee = staffMap[assigned.employee.guid] ?: assigned.employee
            }
            dutyDefinition.rs.forEach { assigned ->
                assigned.employee = staffMap[assigned.employee.guid] ?: assigned.employee
            }
        }

        try {
            augmentHaendWithDocScedTf(
                duties = plan,
                selectedOrgGuid = orgUnitDataGuid,
                from = from,
                to = to
            )
        } catch (e: Exception) {
            Log.w(TAG, "HÄND-Augmentierung fehlgeschlagen: ${e.message}")
        }

        val days = mutableMapOf<String, OrgDay>()
        val keyPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        for (duty in plan) {
            val date = duty.begin.format(keyPattern)
            val day = days.getOrDefault(
                date,
                OrgDay(orgUnitDataGuid, duty.begin, emptyList(), emptyList())
            )

            if (duty.begin.hour <= 16) { // TODO: Check actual day/night-shift differentiation
                day.dayShift = day.dayShift + listOf(duty)
            } else {
                day.nightShift = day.nightShift + listOf(duty)
            }

            days[date] = day
        }

        val daysList = days.values.toList()
        for (day in daysList) {
            day.dayShift = day.dayShift.sortedWith(dutyComparator)
            val lastDayIndex = 1 + day.dayShift.indexOfFirst {
                it.sew.isEmpty()
                        && it.tf.isEmpty()
                        && it.el.isEmpty()
                        && it.rs.isEmpty()
            }
            day.dayShift = if (lastDayIndex > 0) day.dayShift.dropLast(day.dayShift.size - lastDayIndex) else day.dayShift

            day.nightShift = day.nightShift.sortedWith(dutyComparator)
            val lastNightIndex = 1 + day.nightShift.indexOfFirst {
                it.sew.isEmpty()
                        && it.tf.isEmpty()
                        && it.el.isEmpty()
                        && it.rs.isEmpty()
            }

            /*
            if (!duty.el.isEmpty() && !duty.tf.isEmpty() || duty.sew.any { it.requirement == Requirement.HAEND }) {
                timelineItems.add(TimelineItem.Duty(duty))
            }
             */
            day.nightShift = if (lastNightIndex > 0) day.nightShift.dropLast(day.nightShift.size - lastNightIndex) else day.nightShift
        }

        Log.d(TAG, "Loaded ${days.size} days on the timeline")
        return Result.success(daysList)
    }

    suspend fun loadPast(year: String): Result<List<MinimalDutyDefinitionProto>> {
        if (!isLoggedIn) {
            return Result.failure(IOException("Not logged in!"))
        }

        val pastResponse = NetworkService.loadPast(incode!!, year).getOrNull()
        if (pastResponse == null) {
            return Result.failure(IOException("Failed to load past duties!"))
        }

        val pastDuties = DataParserService.parseLoadMinimalDutyDefinitions(JSONObject(pastResponse))
        return Result.success(pastDuties)
    }

    suspend fun loadHoursOfService(year: String): Float {
        val localStats = DataStores.STATISTICS.data.firstOrNull()
        if (localStats != null && !isLoggedIn) {
            return localStats.minutesServed / 60f
        }

        val yearData = loadPast(year).getOrNull()
        if (yearData == null) {
            return 0f
        }

        val minutesServed = yearData.sumOf { it.duration }
        DataStores.STATISTICS.updateData {
            it.toBuilder()
                .setMinutesServed(minutesServed)
                .build()
        }

        return minutesServed / 60f
    }

    suspend fun loadUpcoming(): List<MinimalDutyDefinitionProto> {
        val localUpcoming = DataStores.UPCOMING_DUTIES.data.firstOrNull()
        if (localUpcoming != null && !isLoggedIn) {
            return localUpcoming.minimalDutyDefinitionsList
        }

        val upcomingResponse = NetworkService.loadUpcoming(incode!!).getOrNull()
        if (upcomingResponse == null) {
            return emptyList()
        }

        val upcomingDuties = DataParserService.parseLoadMinimalDutyDefinitions(JSONObject(upcomingResponse))
        DataStores.UPCOMING_DUTIES.updateData {
            it.toBuilder()
                .clearMinimalDutyDefinitions()
                .addAllMinimalDutyDefinitions(upcomingDuties)
                .build()
        }

        return upcomingDuties
    }

    suspend fun loadMessages(
        orgUnitDataGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<Message>> {
        if (!isLoggedIn) {
            return Result.failure(IOException("Not logged in!"))
        }

        val resourcesResponse =
            NetworkService.getResources(incode!!, orgUnitDataGuid, from, to).getOrNull()
        if (resourcesResponse == null) {
            return Result.failure(IOException("Failed to get resources!"))
        }
        val resources = DataParserService.parseGetResources(JSONObject(resourcesResponse))
        if (resources == null) {
            return Result.failure(IOException("Failed to collect resources!"))
        }

        val messagesResponse =
            NetworkService.getMessages(incode!!, orgUnitDataGuid, from, to).getOrNull()
        if (messagesResponse == null) {
            return Result.failure(IOException("Failed to get messages!"))
        }
        val messages = DataParserService.parseGetMessages(JSONObject(messagesResponse))
        if (messages == null) {
            return Result.failure(IOException("Failed to collect messages!"))
        }

        for (message in messages) {
            val matchingResource = resources.firstOrNull { it.messagesGuid == message.resourceGuid }
            if (matchingResource == null) {
                Log.w(TAG, "Message without resource. Skipping...")
                continue
            }

            val messageList = this.messages.getOrDefault(matchingResource.employeeGuid, emptyList())
                .toMutableList()
            if (messageList.find { it.guid == message.guid } == null) {
                messageList.add(message)
            }
            this.messages[matchingResource.employeeGuid] = messageList
        }

        Log.d(TAG, "Loaded ${messages.size} messages")
        return Result.success(messages)
    }

    private suspend fun augmentHaendWithDocScedTf(
        duties: List<DutyDefinition>,
        selectedOrgGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ) {
        val config = Org.parse(selectedOrgGuid)
        if (config == null || config == Org.INVALID) {
            Log.d(TAG, "DocSced: ausgewählte Org ist kein HÄND (Wels/Wels-Land) – skip")
            return
        }

        val html = NetworkService.loadDocScedCalendar(config = config, gran = "ges").getOrNull()
        if (html.isNullOrBlank()) {
            Log.w(TAG, "DocSced: kein HTML geladen für $config")
            return
        }
        val days = DocScedParserService.parseFahrdienst(html).getOrNull().orEmpty()
        val byDate = days.associateBy { it.date } // Map<LocalDate, FahrdienstDay>
        Log.d(TAG, "DocSced: ${days.size} Fahrdienst-Tage geparst für $config")

        for (duty in duties) {
            val haends = duty.sew.filter { it.requirement == Requirement.HAEND }
            if (haends.isEmpty()) continue

            for (haend in haends) {
                val zone = ZoneId.systemDefault()

                val mid = localMidpoint(haend.begin, haend.end, zone)
                val night = isNight(mid)
                val dsDate = docscedRowDate(mid)

                val dsDay = byDate[dsDate]
                if (dsDay == null) {
                    Log.d(TAG, "DocSced: kein Zeilendatum $dsDate ($config)")
                    continue
                }

                Log.d(
                    TAG,
                    "DocSced[$config][$dsDate] TAG=${dsDay.tag.joinToString()} | NACHT=${dsDay.nacht.joinToString()}"
                )

                val candidate = if (night) dsDay.nacht.firstOrNull() else dsDay.tag.firstOrNull()
                if (candidate.isNullOrBlank()) {
                    Log.d(
                        TAG,
                        "DocSced: kein ${if (night) "Nacht" else "Tag"}-Arzt am $dsDate ($config)"
                    )
                    continue
                }

                //val label = "HÄND ${stringResource(config.getResourceString())} (${if (night) "Nacht" else "Tag"})"
                val label = "HÄND ${if (config == Org.WELS_LAND) "Wels-Land" else "Wels-Stadt"} (${if (night) "Nacht" else "Tag"})"
                val docEmployee = Employee(
                    guid = "docsced:$config:$dsDate:${if (night) "nacht" else "tag"}",
                    name = candidate,
                    //identifier = stringResource(R.string.data_requirement_haend_dr)
                    identifier = "Dr."
                )

                val targetTf =
                    duty.tf.firstOrNull { overlaps(it.begin, it.end, haend.begin, haend.end) }
                        ?: duty.tf.firstOrNull()
                if (targetTf != null) {
                    targetTf.employee = docEmployee
                    targetTf.info =
                        (targetTf.info + if (targetTf.info.isBlank()) label else " | $label").trim()
                    targetTf.requirement = Requirement.HAEND_DR
                } else {
                    duty.tf.add(
                        AssignedEmployee(
                            employee = docEmployee,
                            begin = haend.begin,
                            end = haend.end,
                            requirement = Requirement.HAEND_DR,
                            info = label
                        )
                    )
                }

                Log.d(
                    TAG,
                    "DocSced: ${docEmployee.name} → TF gesetzt ($config, $dsDate, ${if (night) "Nacht" else "Tag"}) " +
                            "für HÄND ${
                                haend.begin.atZoneSameInstant(zone).toLocalTime()
                            }–${haend.end.atZoneSameInstant(zone).toLocalTime()}"
                )

            }
        }
    }

    suspend fun createAndAllocateDuty(planDataGuid: String): Result<CreateDutyResponse> {
        val code = getIncode() ?: return Result.failure(IOException("Not logged in!"))

        val body = NetworkService.createAndAllocateDuty(code, planDataGuid).getOrNull()
            ?: return Result.failure(IOException("Failed to create duty!"))

        val parsed = try {
            DataParserService.parseCreateAndAllocateDuty(JSONObject(body))
        } catch (e: Exception) {
            Log.e(TAG, "Invalid JSON! $body")
            null
        }

        return if (parsed == null) {
            Result.failure(IOException("Failed to parse create duty response!"))
        } else {
            Result.success(parsed)
        }
    }

    private fun localMidpoint(b: OffsetDateTime, e: OffsetDateTime, zone: ZoneId): ZonedDateTime {
        val bl = b.atZoneSameInstant(zone)
        val el = e.atZoneSameInstant(zone)
        val dur = Duration.between(bl, el)      // korrekt auch über Mitternacht & DST
        return bl.plus(dur.dividedBy(2))
    }

    private fun isNight(mid: ZonedDateTime): Boolean {
        val t = mid.toLocalTime()
        return t < LocalTime.of(6, 0) || t >= LocalTime.of(18, 0)
    }

    /** DocSced zeigt die Nachtdienste in der Zeile des ABENDS.
     *  Liegt der Midpoint < 06:00, ordnen wir ihn dem VORTAG zu.
     */
    private fun docscedRowDate(mid: ZonedDateTime): LocalDate {
        val t = mid.toLocalTime()
        return if (t < LocalTime.of(6, 0)) mid.toLocalDate().minusDays(1) else mid.toLocalDate()
    }

    private fun overlaps(
        aStart: OffsetDateTime,
        aEnd: OffsetDateTime,
        bStart: OffsetDateTime,
        bEnd: OffsetDateTime
    ): Boolean {
        val aS = aStart.toInstant()
        val aE = aEnd.toInstant()
        val bS = bStart.toInstant()
        val bE = bEnd.toInstant()
        return aS < bE && bS < aE
    }

    private fun isNight(begin: LocalTime, end: LocalTime): Boolean {
        return begin >= LocalTime.of(19, 0) || end <= LocalTime.of(7, 0)
    }
}

