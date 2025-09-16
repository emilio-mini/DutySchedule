package me.emiliomini.dutyschedule.services.prep.live

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.datastore.prep.IncodeProto
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyGroupProto
import me.emiliomini.dutyschedule.datastore.prep.duty.MinimalDutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeItemsProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.datastore.prep.employee.SlotProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgDayProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.debug.DebugFlags
import me.emiliomini.dutyschedule.enums.NetworkTarget
import me.emiliomini.dutyschedule.json.util.s
import me.emiliomini.dutyschedule.models.network.CreateDutyResponse
import me.emiliomini.dutyschedule.models.prep.Message
import me.emiliomini.dutyschedule.models.prep.Org
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.services.network.DataExtractorService
import me.emiliomini.dutyschedule.services.network.DataParserService
import me.emiliomini.dutyschedule.services.network.NetworkService
import me.emiliomini.dutyschedule.services.parsers.DocScedParserService
import me.emiliomini.dutyschedule.services.prep.ScheduleService
import me.emiliomini.dutyschedule.services.storage.DataKeys
import me.emiliomini.dutyschedule.services.storage.DataStores
import me.emiliomini.dutyschedule.services.storage.StorageService
import me.emiliomini.dutyschedule.util.format
import me.emiliomini.dutyschedule.util.getAllVehicles
import me.emiliomini.dutyschedule.util.getAllocatedSlotsCount
import me.emiliomini.dutyschedule.util.getVehicle
import me.emiliomini.dutyschedule.util.isNightShift
import me.emiliomini.dutyschedule.util.toOffsetDateTime
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONException
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.collections.firstOrNull
import kotlin.collections.isNotEmpty

object PrepService : ScheduleService {
    private const val TAG = "PrepService"

    private var incode: IncodeProto? = null

    private val messages = mutableMapOf<String, List<Message>>()

    private val dutyComparator = Comparator<DutyDefinitionProto> { d1, d2 ->
        val d1Sew = d1.getVehicle()
        val d2Sew = d2.getVehicle()
        val d1HasSew = d1Sew != null
        val d2HasSew = d2Sew != null

        if (d1.hasGroupGuid() && !d2.hasGroupGuid()) return@Comparator 1
        if (!d1.hasGroupGuid() && d2.hasGroupGuid()) return@Comparator -1

        if (d1.hasGroupGuid() && d2.hasGroupGuid()) {
            val gGuid1 = d1.groupGuid
            val gGuid2 = d2.groupGuid
            val groupCmp = gGuid1.compareTo(gGuid2)
            if (groupCmp != 0) return@Comparator groupCmp
        }

        if (d1HasSew && !d2HasSew) return@Comparator -1
        if (!d1HasSew && d2HasSew) return@Comparator 1

        if (d1HasSew && d2HasSew) {
            val name1 = d1Sew.inlineEmployee.name
            val name2 = d2Sew.inlineEmployee.name
            val nameCmp = name1.compareTo(name2)
            if (nameCmp != 0) return@Comparator nameCmp
        }

        val d1Count = d1.getAllocatedSlotsCount()
        val d2Count = d2.getAllocatedSlotsCount()
        return@Comparator d1Count.compareTo(d2Count)
    }

    override var isLoggedIn by mutableStateOf(false);
    override var self by mutableStateOf<EmployeeProto?>(null)

    override fun getMessages(): Map<String, List<Message>> {
        return this.messages
    }

    override fun getIncode(): IncodeProto? {
        return this.incode
    }

    override suspend fun getOrg(abbreviationOrIdentifier: String): OrgProto? {
        val orgs = this.loadOrgs()?.orgsMap?.values ?: return null

        var org = orgs.firstOrNull { it.abbreviation == abbreviationOrIdentifier }
        if (org == null) {
            org = orgs.firstOrNull { it.identifier == abbreviationOrIdentifier }
        }

        return org
    }

    override suspend fun login(username: String, password: String): Boolean {
        val loginResult: Result<String?> = if (DebugFlags.AVOID_PREP_API.active()) {
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

    override suspend fun previouslyLoggedIn(): Boolean {
        val username = StorageService.load(DataKeys.USERNAME)
        val password = StorageService.load(DataKeys.PASSWORD)

        return username != null && password != null
    }

    override suspend fun restoreLogin(): Boolean {
        val username = StorageService.load(DataKeys.USERNAME)
        val password = StorageService.load(DataKeys.PASSWORD)
        val incode = DataStores.INCODE.data.firstOrNull()
        val maxAge = 5L * 60L * 1_000L

        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            return false
        }

        if (DebugFlags.AVOID_PREP_API.active()) {
            return this.login(username, password)
        }

        val lastIncodeUseMillis =
            incode?.lastUsed?.toOffsetDateTime()?.toInstant()?.toEpochMilli() ?: 0L
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

    override suspend fun logout() {
        this.incode = null
        DataStores.clearAndReset()
    }

    override suspend fun loadSelf(guid: String?, org: String?): EmployeeProto? {
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
            this.self = staffResult.firstOrNull { it.guid == guid }
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

    override suspend fun loadOrgs(): OrgItemsProto? {
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

            val orgTreeUrl = NetworkTarget.Companion.withScheduleBase(orgTreeLocation).toHttpUrl()
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

    override suspend fun loadAllowedOrgs(): List<String>? {
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

    override suspend fun loadPlan(
        orgUnitDataGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<Pair<List<DutyDefinitionProto>, Map<String, DutyGroupProto>>> {
        Log.d(TAG, "Loading plan...")

        if (!this.isLoggedIn) {
            return Result.failure(okio.IOException("Not logged in!"))
        }

        if (DebugFlags.AVOID_PREP_API.active()) { // TODO: Replace with demo mode
            return Result.success(Pair(emptyList(), emptyMap()))
        }

        val planBody = NetworkService.loadPlan(incode!!, orgUnitDataGuid, from, to).getOrNull()
        if (planBody.isNullOrBlank()) {
            return Result.failure(okio.IOException("Failed to load plan!"))
        }

        try {
            val result = DataParserService.parseLoadPlan(JSONObject(planBody))
            return Result.success(result)
        } catch (e: JSONException) {
            Log.e(TAG, "Invalid JSON! $planBody")
            return Result.failure(e)
        }
    }

    override suspend fun getStaff(
        orgUnitDataGuid: String,
        staffDataGuid: List<String>,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<EmployeeProto>> {
        Log.d(TAG, "Getting staff...")

        if (!this.isLoggedIn) {
            return Result.failure(okio.IOException("Not logged in!"))
        }

        if (DebugFlags.AVOID_PREP_API.active()) { // TODO: Replace with demo mode
            return Result.success(emptyList())
        }

        val staffBody =
            NetworkService.getStaff(incode!!, orgUnitDataGuid, staffDataGuid, from, to).getOrNull()
        if (staffBody.isNullOrBlank()) {
            return Result.failure(okio.IOException("Failed to get staff!"))
        }

        try {
            val staff = DataParserService.parseGetStaff(JSONObject(staffBody))
            DataStores.EMPLOYEES.updateData {
                it.toBuilder().putAllEmployees(staff.associateBy { it.guid }).build()
            }
            return Result.success(staff)
        } catch (e: JSONException) {
            Log.e(TAG, "Invalid JSON! $staffBody")
            return Result.failure(e)
        }
    }

    override suspend fun loadTimeline(
        orgUnitDataGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<OrgDayProto>> {
        // Load plan
        var (duties, groups) = this.loadPlan(orgUnitDataGuid, from, to).getOrDefault(Pair(emptyList(), emptyMap()))
        if (duties.isEmpty()) {
            Log.e(TAG, "Failed to load plan")
            return Result.failure(okio.IOException("Failed to load plan!"))
        }

        // Ensure staff is loaded
        val employeeGuids = duties.flatMap { it.slotsList.map {
            if (it.hasEmployeeGuid()) it.employeeGuid else null
        } }.filterNotNull().distinct()
        val localEmployees =
            DataStores.EMPLOYEES.data.firstOrNull() ?: EmployeeItemsProto.getDefaultInstance()
        val missingEmployeeGuids =
            employeeGuids.filter { !localEmployees.employeesMap.contains(it) }
        Log.d(TAG, "Skipping ${localEmployees.employeesCount} local employees")
        if (missingEmployeeGuids.isNotEmpty()) {
            coroutineScope {
                launch {
                    val staff =
                        getStaff(orgUnitDataGuid, missingEmployeeGuids, from, to).getOrNull()
                    if (staff.isNullOrEmpty()) {
                        Log.e(TAG, "Failed to load missing staff $missingEmployeeGuids")
                    } else {
                        Log.d(TAG, "Loaded ${staff.size} staff elements")
                    }
                }
            }
        }

        try {
            duties = augmentHaendWithDocScedTf(
                duties = duties,
                selectedOrgGuid = orgUnitDataGuid,
                from = from,
                to = to
            )
        } catch (e: Exception) {
            Log.w(TAG, "HÄND-Augmentierung fehlgeschlagen: ${e.message}")
        }

        val days = mutableMapOf<String, OrgDayProto>()
        for (duty in duties) {
            val date = duty.begin.format("yyyy-MM-dd")
            val day = days.getOrDefault(
                date,
                OrgDayProto.newBuilder()
                    .setOrgGuid(orgUnitDataGuid)
                    .setDate(duty.begin)
                    .build()
            )

            if (duty.isNightShift()) {
                days[date] = day.toBuilder()
                    .s(duty.groupGuid, additionalCondition = { it.isNotBlank() && groups.containsKey(it) }) { putGroups(it, groups.getValue(it)) }
                    .addNightShift(duty)
                    .build()
            } else {
                days[date] = day.toBuilder()
                    .s(duty.groupGuid, additionalCondition = { it.isNotBlank() && groups.containsKey(it) }) { putGroups(it, groups.getValue(it)) }
                    .addDayShift(duty)
                    .build()
            }
        }

        var daysList = days.values.toList()
        daysList = daysList.map {
            var dayShifts = it.dayShiftList.toList().sortedWith(dutyComparator)
            /*val lastDayShiftIndex = 1 + dayShifts.indexOfFirst {
                it.getAllocatedSlotsCount() == 0
            }
            dayShifts =
                if (lastDayShiftIndex > 0) dayShifts.dropLast(dayShifts.size - lastDayShiftIndex) else dayShifts*/

            var nightShifts = it.nightShiftList.toList().sortedWith(dutyComparator)
            /*val lastNightShiftIndex = 1 + nightShifts.indexOfFirst {
                it.getAllocatedSlotsCount() == 0
            }
            nightShifts =
                if (lastNightShiftIndex > 0) nightShifts.dropLast(nightShifts.size - lastNightShiftIndex) else nightShifts*/

            it.toBuilder()
                .clearDayShift()
                .clearNightShift()
                .addAllDayShift(dayShifts)
                .addAllNightShift(nightShifts)
                .build()
        }

        Log.d(TAG, "Loaded ${days.size} days on the timeline")
        return Result.success(daysList)
    }

    override suspend fun loadPast(year: String): Result<List<MinimalDutyDefinitionProto>> {
        if (!isLoggedIn) {
            return Result.failure(okio.IOException("Not logged in!"))
        }

        val pastResponse = NetworkService.loadPast(incode!!, year).getOrNull()
        if (pastResponse == null) {
            return Result.failure(okio.IOException("Failed to load past duties!"))
        }

        val pastDuties = DataParserService.parseLoadMinimalDutyDefinitions(JSONObject(pastResponse))
        return Result.success(pastDuties)
    }

    override suspend fun loadHoursOfService(year: String): Float {
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

    override suspend fun loadUpcoming(): List<MinimalDutyDefinitionProto> {
        val localUpcoming = DataStores.UPCOMING_DUTIES.data.firstOrNull()
        if (localUpcoming != null && !isLoggedIn) {
            return localUpcoming.minimalDutyDefinitionsList
        }

        val upcomingResponse = NetworkService.loadUpcoming(incode!!).getOrNull()
        if (upcomingResponse == null) {
            return emptyList()
        }

        val upcomingDuties = DataParserService.parseLoadMinimalDutyDefinitions(
            JSONObject(
                upcomingResponse
            )
        )
        DataStores.UPCOMING_DUTIES.updateData {
            it.toBuilder()
                .clearMinimalDutyDefinitions()
                .addAllMinimalDutyDefinitions(upcomingDuties)
                .build()
        }

        return upcomingDuties
    }

    override suspend fun loadMessages(
        orgUnitDataGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<Message>> {
        if (!isLoggedIn) {
            return Result.failure(okio.IOException("Not logged in!"))
        }

        val messagesResponse =
            NetworkService.getMessages(incode!!, orgUnitDataGuid, from, to).getOrNull()
        if (messagesResponse == null) {
            return Result.failure(okio.IOException("Failed to get messages!"))
        }
        val messages = DataParserService.parseGetMessages(JSONObject(messagesResponse))
        if (messages == null) {
            return Result.failure(okio.IOException("Failed to collect messages!"))
        }

        for (message in messages) {
            val messageList = this.messages.getOrDefault(message.resourceGuid, emptyList())
                .toMutableList()
            if (messageList.find { it.guid == message.guid } == null) {
                messageList.add(message)
            }
            this.messages[message.resourceGuid] = messageList
        }

        Log.d(TAG, "Loaded ${messages.size} messages")
        return Result.success(messages)
    }

    override suspend fun createAndAllocateDuty(planDataGuid: String): Result<CreateDutyResponse> {
        val code = getIncode() ?: return Result.failure(okio.IOException("Not logged in!"))

        val body = NetworkService.createAndAllocateDuty(code, planDataGuid).getOrNull()
            ?: return Result.failure(okio.IOException("Failed to create duty!"))

        val parsed = try {
            DataParserService.parseCreateAndAllocateDuty(JSONObject(body))
        } catch (e: Exception) {
            Log.e(TAG, "Invalid JSON! $body")
            null
        }

        return if (parsed == null) {
            Result.failure(okio.IOException("Failed to parse create duty response!"))
        } else {
            Result.success(parsed)
        }
    }

    private suspend fun augmentHaendWithDocScedTf(
        duties: List<DutyDefinitionProto>,
        selectedOrgGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): List<DutyDefinitionProto> {
        val config = Org.Companion.parse(selectedOrgGuid)
        if (config == null || config == Org.INVALID) {
            Log.d(TAG, "DocSced: ausgewählte Org ist kein HÄND (Wels/Wels-Land) – skip")
            return duties
        }

        val html = NetworkService.loadDocScedCalendar(config = config, gran = "ges").getOrNull()
        if (html.isNullOrBlank()) {
            Log.w(TAG, "DocSced: kein HTML geladen für $config")
            return duties
        }
        val days = DocScedParserService.parseFahrdienst(html).getOrNull().orEmpty()
        val byDate = days.associateBy { it.date } // Map<LocalDate, FahrdienstDay>
        Log.d(TAG, "DocSced: ${days.size} Fahrdienst-Tage geparst für $config")

        val mutableDuties = duties.toMutableList()
        for (i in mutableDuties.indices) {
            val duty = mutableDuties[i]
            val haends = duty.getAllVehicles(listOf(Requirement.HAEND.value))
            if (haends.isEmpty()) continue

            for (haend in haends) {
                val zone = ZoneId.systemDefault()

                val mid = localMidpoint(
                    haend.begin.toOffsetDateTime(),
                    haend.end.toOffsetDateTime(),
                    zone
                )
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

                val docEmployee = EmployeeProto.newBuilder()
                    .setGuid("docsced:$config:$dsDate:${if (night) "nacht" else "tag"}")
                    .setName(candidate)
                    .setIdentifier("Dr.")
                    .build()

                mutableDuties[i] = duty.toBuilder()
                    .addSlots(
                        SlotProto.newBuilder()
                            .setBegin(haend.begin)
                            .setEnd(haend.end)
                            .setRequirement(
                                RequirementProto.newBuilder().setGuid(Requirement.HAEND_DR.value)
                                    .build()
                            )
                            .setInlineEmployee(docEmployee)
                            .build()
                    )
                    .build()

                Log.d(
                    TAG,
                    "DocSced: ${docEmployee.name} → TF gesetzt ($config, $dsDate, ${if (night) "Nacht" else "Tag"}) " +
                            "für HÄND ${
                                haend.begin.format("yyyy-MM-dd HH:mm")
                            }–${haend.end.format("yyyy-MM-dd HH:mm")}"
                )

            }
        }

        return mutableDuties.toList()
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
}