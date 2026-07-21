@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.services.prep.demo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.emiliomini.dutyschedule.shared.datastores.CreateDutyResponse
import me.emiliomini.dutyschedule.shared.datastores.DutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.DutyGroup
import me.emiliomini.dutyschedule.shared.datastores.DutyType
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.EmployeeItems
import me.emiliomini.dutyschedule.shared.datastores.Incode
import me.emiliomini.dutyschedule.shared.datastores.Message
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Org
import me.emiliomini.dutyschedule.shared.datastores.OrgDay
import me.emiliomini.dutyschedule.shared.datastores.OrgItems
import me.emiliomini.dutyschedule.shared.datastores.Requirement
import me.emiliomini.dutyschedule.shared.datastores.Slot
import me.emiliomini.dutyschedule.shared.datastores.Statistics
import me.emiliomini.dutyschedule.shared.datastores.UpcomingDutyItems
import me.emiliomini.dutyschedule.shared.mappings.MappedSkills
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleServiceBase
import me.emiliomini.dutyschedule.shared.services.prep.live.PrepService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.isNightShift
import me.emiliomini.dutyschedule.shared.util.toInstant
import me.emiliomini.dutyschedule.shared.util.toTimestamp
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Offline, in-memory implementation of [DutyScheduleServiceBase] backing the onboarding
 * "Demo Mode" entry point. It never talks to the network. Sample data is generated once on
 * activation and also mirrored into [StorageService] (the same way [PrepService] caches its
 * data) since several screens read their state from there directly. [logout] wipes it again.
 */
object DemoService : DutyScheduleServiceBase {
    override var isLoggedIn by mutableStateOf(false)
    override var self by mutableStateOf<Employee?>(null)

    private val zone = TimeZone.currentSystemDefault()
    private const val PAST_WINDOW_DAYS = 3 * 365
    private const val FUTURE_WINDOW_DAYS = 90

    val org = Org(
        guid = "demo-org-musterstadt",
        title = "Bereitschaft Musterstadt",
        abbreviation = "MUSTERSTADT",
        identifier = "demo-musterstadt"
    )
    val secondOrg = Org(
        guid = "demo-org-nachbarort",
        title = "Bereitschaft Nachbarort",
        abbreviation = "NACHBARORT",
        identifier = "demo-nachbarort"
    )

    private val selfEmployee = Employee(
        guid = "demo-self",
        name = "Max Mustermann",
        identifier = "Demo-Account",
        phone = "+43 664 1234567",
        email = "max.mustermann@example.com",
        defaultOrg = org.guid,
        birthdate = Instant.parse("1995-06-12T00:00:00Z").toTimestamp(),
        resourceTypeGuid = "",
        skills = listOf(MappedSkills.NFS.asSkill(), MappedSkills.RS.asSkill())
    )

    private val colleagues = listOf(
        Employee(
            guid = "demo-colleague-1",
            name = "Anna Schmidt",
            identifier = "23456",
            phone = "+43 664 2345678",
            defaultOrg = org.guid,
            skills = listOf(MappedSkills.RS.asSkill())
        ),
        Employee(
            guid = "demo-colleague-2",
            name = "Peter Huber",
            identifier = "34567",
            phone = "+43 664 3456789",
            defaultOrg = org.guid,
            skills = listOf(MappedSkills.NFS.asSkill())
        ),
        Employee(
            guid = "demo-colleague-3",
            name = "Lisa Bauer",
            identifier = "45678",
            phone = "+43 664 4567890",
            defaultOrg = org.guid,
            skills = listOf(MappedSkills.RS.asSkill())
        ),
        Employee(
            guid = "demo-colleague-4",
            name = "Tom Wagner",
            identifier = "56789",
            phone = "+43 664 5678901",
            defaultOrg = secondOrg.guid,
            skills = listOf(MappedSkills.RS.asSkill())
        )
    )

    private val employeesByGuid = (colleagues + selfEmployee).associateBy { it.guid }

    private val messagesByResource = mapOf(
        org.guid to listOf(
            Message(
                guid = "demo-msg-org",
                resourceGuid = org.guid,
                title = "Willkommen im Demo-Modus",
                message = "Diese Ansicht zeigt frei erfundene Beispieldaten. Es besteht keine Verbindung zu einem echten Dienstplan.",
                priority = 1
            )
        ),
        selfEmployee.guid to listOf(
            Message(
                guid = "demo-msg-self",
                resourceGuid = selfEmployee.guid,
                title = "Ausrüstung prüfen",
                message = "Bitte die Fahrzeugausrüstung vor Dienstbeginn kontrollieren.",
                priority = 0
            )
        )
    )

    private data class GeneratedDuty(
        val definition: DutyDefinition,
        val type: DutyType,
        val typeString: String
    )

    private val dutiesByGuid = LinkedHashMap<String, GeneratedDuty>()
    private val dutyGuidBySlotGuid = mutableMapOf<String, String>()
    private var generated = false
    private val generationMutex = Mutex()

    override fun getMessages(): Map<String, List<Message>> {
        return messagesByResource
    }

    override fun getIncode(): Incode {
        return Incode(token = "demo", value = "demo-session", lastUsed = Clock.System.now().toTimestamp())
    }

    override suspend fun getOrg(abbreviationOrIdentifier: String): Org? {
        return listOf(org, secondOrg).firstOrNull {
            it.abbreviation == abbreviationOrIdentifier || it.identifier == abbreviationOrIdentifier || it.guid == abbreviationOrIdentifier
        }
    }

    override suspend fun login(username: String, password: String): Boolean {
        activate()
        return true
    }

    override suspend fun previouslyLoggedIn(): Boolean {
        return false
    }

    override suspend fun restoreLogin(): Boolean {
        activate()
        return true
    }

    override suspend fun logout() {
        self = null
        isLoggedIn = false
        StorageService.clear()
        DutyScheduleService = PrepService
    }

    override suspend fun loadSelf(guid: String?, org: String?): Employee {
        return selfEmployee
    }

    override suspend fun loadOrgs(): OrgItems {
        return OrgItems(orgs = mapOf(org.guid to org, secondOrg.guid to secondOrg))
    }

    override suspend fun loadAllowedOrgs(): List<String> {
        return listOf(org.guid, secondOrg.guid)
    }

    override suspend fun loadPlan(
        orgUnitDataGuid: String,
        from: Instant,
        to: Instant
    ): Pair<List<DutyDefinition>, Map<String, DutyGroup>> {
        ensureGenerated()
        if (orgUnitDataGuid != org.guid) {
            return Pair(emptyList(), emptyMap())
        }

        val duties = dutiesByGuid.values
            .map { it.definition }
            .filter { it.begin.toInstant() < to && it.end.toInstant() > from }
        return Pair(duties, emptyMap())
    }

    override suspend fun getStaff(
        orgUnitDataGuid: String,
        staffDataGuid: List<String>,
        from: Instant,
        to: Instant
    ): List<Employee> {
        return staffDataGuid.mapNotNull { employeesByGuid[it] }
    }

    override suspend fun loadTimeline(
        orgUnitDataGuid: String,
        from: Instant,
        to: Instant
    ): List<OrgDay> {
        val (duties, _) = loadPlan(orgUnitDataGuid, from, to)

        val days = mutableMapOf<String, OrgDay>()
        for (duty in duties) {
            val dateKey = duty.begin.toInstant().toLocalDateTime(zone).date.toString()
            val day = days.getOrElse(dateKey) { OrgDay(orgUnitDataGuid, duty.begin) }
            days[dateKey] = if (duty.isNightShift(zone)) {
                day.copy(nightShifts = day.nightShifts + duty)
            } else {
                day.copy(dayShifts = day.dayShifts + duty)
            }
        }

        return days.values.sortedBy { it.date.toInstant() }
    }

    override suspend fun loadPast(year: String): List<MinimalDutyDefinition> {
        ensureGenerated()
        val intYear = year.toIntOrNull() ?: return emptyList()
        val now = Clock.System.now()
        return selfDuties()
            .filter { it.definition.begin.toInstant() < now }
            .filter { it.definition.begin.toInstant().toLocalDateTime(zone).year == intYear }
            .sortedByDescending { it.definition.begin.toInstant() }
            .map { it.toMinimal() }
    }

    override suspend fun loadHoursOfService(year: String): Float {
        val minutesServed = loadPast(year).sumOf { it.duration }
        return minutesServed / 60f
    }

    override suspend fun loadUpcoming(): List<MinimalDutyDefinition> {
        ensureGenerated()
        val now = Clock.System.now()
        return selfDuties()
            .filter { it.definition.begin.toInstant() >= now }
            .sortedBy { it.definition.begin.toInstant() }
            .map { it.toMinimal() }
    }

    override suspend fun loadMessages(
        orgUnitDataGuid: String,
        from: Instant,
        to: Instant
    ): List<Message> {
        return messagesByResource[org.guid].orEmpty()
    }

    override suspend fun createAndAllocateDuty(planDataGuid: String): CreateDutyResponse {
        ensureGenerated()
        val dutyGuid = dutyGuidBySlotGuid[planDataGuid]
            ?: return CreateDutyResponse(success = false, errorMessages = listOf("Unbekannter Dienstplatz."))
        val generatedDuty = dutiesByGuid[dutyGuid]
            ?: return CreateDutyResponse(success = false, errorMessages = listOf("Unbekannter Dienstplatz."))
        val slot = generatedDuty.definition.slots.firstOrNull { it.guid == planDataGuid }
            ?: return CreateDutyResponse(success = false, errorMessages = listOf("Unbekannter Dienstplatz."))

        if (!slot.employeeGuid.isNullOrBlank()) {
            return CreateDutyResponse(
                success = false,
                errorMessages = listOf("Dieser Dienstplatz ist bereits vergeben.")
            )
        }

        val updatedSlots = generatedDuty.definition.slots.map {
            if (it.guid == planDataGuid) it.copy(employeeGuid = selfEmployee.guid, inlineEmployee = selfEmployee) else it
        }
        dutiesByGuid[dutyGuid] = generatedDuty.copy(definition = generatedDuty.definition.copy(slots = updatedSlots))

        return CreateDutyResponse(success = true, successMessage = "Dienst erfolgreich eingetragen.")
    }

    private fun selfDuties() = dutiesByGuid.values.filter { generatedDuty ->
        generatedDuty.definition.slots.any { it.employeeGuid == selfEmployee.guid }
    }

    private fun GeneratedDuty.toMinimal(): MinimalDutyDefinition {
        val vehicleSlot = definition.slots.firstOrNull { RequirementMapping.VEHICLES.contains(it.requirement.guid) && !it.employeeGuid.isNullOrBlank() }
        val staffNames = definition.slots
            .filter { !it.employeeGuid.isNullOrBlank() && !RequirementMapping.VEHICLES.contains(it.requirement.guid) }
            .mapNotNull { employeesByGuid[it.employeeGuid]?.name ?: it.inlineEmployee?.name?.ifBlank { null } }
        val minutes =
            ((definition.end.toInstant().toEpochMilliseconds() - definition.begin.toInstant().toEpochMilliseconds()) / 60_000L).toInt()

        return MinimalDutyDefinition(
            guid = definition.guid,
            begin = definition.begin,
            end = definition.end,
            type = type,
            vehicle = vehicleSlot?.inlineEmployee?.name,
            staff = staffNames,
            duration = minutes,
            typeString = typeString
        )
    }

    private suspend fun activate() {
        ensureGenerated()
        self = selfEmployee
        isLoggedIn = true
        seedStorage()
    }

    private suspend fun ensureGenerated() {
        if (generated) return
        generationMutex.withLock {
            if (generated) return@withLock
            generate()
            generated = true
        }
    }

    private fun LocalDate.atStartOfDayInstant(): Instant {
        return LocalDateTime(this, LocalTime(0, 0, 0, 0)).toInstant(zone)
    }

    private fun generate() {
        val today = Clock.System.now().toLocalDateTime(zone).date
        val selfAssignedOffsets = (-PAST_WINDOW_DAYS..30 step 5).toSet()
        val openOffsets = setOf(1, 2, 4, 7, 10, 14).filterNot { selfAssignedOffsets.contains(it) }.toSet()

        for (offset in -PAST_WINDOW_DAYS..FUTURE_WINDOW_DAYS) {
            val date = today.plus(offset, DateTimeUnit.DAY)
            val rnd = Random(offset * 7919 + 13)
            val assignSelf = selfAssignedOffsets.contains(offset)

            val generatedDuty = when {
                offset.mod(9) == 4 -> buildTrainingDuty(date, offset, rnd, assignSelf)
                offset.mod(13) == 6 -> buildMeetingDuty(date, offset, rnd, assignSelf)
                else -> buildEmsDuty(date, offset, rnd, assignSelf, openOffsets.contains(offset))
            }

            dutiesByGuid[generatedDuty.definition.guid] = generatedDuty
            generatedDuty.definition.slots.forEach { dutyGuidBySlotGuid[it.guid] = generatedDuty.definition.guid }
        }
    }

    private fun buildEmsDuty(
        date: LocalDate,
        offset: Int,
        rnd: Random,
        assignSelf: Boolean,
        leaveOpen: Boolean
    ): GeneratedDuty {
        val night = offset.mod(2) == 1
        val begin = date.atStartOfDayInstant().plus(if (night) 19 else 7, DateTimeUnit.HOUR)
        val end = begin.plus(12, DateTimeUnit.HOUR)

        val dutyGuid = "demo-duty-ems-$offset"
        val vehicleCallSign = "RTB Musterstadt ${1 + offset.mod(3)}"
        val vehicleAssigned = rnd.nextInt(100) < 92
        val driver = colleagues[offset.mod(colleagues.size)]
        val passengerCandidate = colleagues.first { it.skills.contains(MappedSkills.NFS.asSkill()) }
        val teammate = colleagues[(offset + 1).mod(colleagues.size)]
        val teammateAssigned = rnd.nextInt(100) < 85

        val slots = listOf(
            Slot(
                guid = "$dutyGuid-vehicle",
                employeeGuid = if (vehicleAssigned) vehicleCallSign else null,
                requirement = Requirement(RequirementMapping.RTW.value),
                begin = begin.toTimestamp(),
                end = end.toTimestamp(),
                inlineEmployee = if (vehicleAssigned) Employee(guid = vehicleCallSign, name = vehicleCallSign) else null
            ),
            Slot(
                guid = "$dutyGuid-driver",
                employeeGuid = driver.guid,
                requirement = Requirement(RequirementMapping.EL.value),
                begin = begin.toTimestamp(),
                end = end.toTimestamp(),
                inlineEmployee = driver
            ),
            Slot(
                guid = "$dutyGuid-passenger",
                employeeGuid = if (assignSelf) selfEmployee.guid else if (leaveOpen) null else passengerCandidate.guid,
                requirement = Requirement(RequirementMapping.RTW_NFS.value),
                begin = begin.toTimestamp(),
                end = end.toTimestamp(),
                inlineEmployee = if (assignSelf) selfEmployee else if (leaveOpen) null else passengerCandidate
            ),
            Slot(
                guid = "$dutyGuid-teammate",
                employeeGuid = if (teammateAssigned) teammate.guid else null,
                requirement = Requirement(RequirementMapping.RTW_RS.value),
                begin = begin.toTimestamp(),
                end = end.toTimestamp(),
                inlineEmployee = if (teammateAssigned) teammate else null
            )
        )

        val definition = DutyDefinition(
            guid = dutyGuid,
            begin = begin.toTimestamp(),
            end = end.toTimestamp(),
            slots = slots
        )

        return GeneratedDuty(definition, DutyType.EMS, "[ RTW ]")
    }

    private fun buildTrainingDuty(date: LocalDate, offset: Int, rnd: Random, assignSelf: Boolean): GeneratedDuty {
        val begin = date.atStartOfDayInstant().plus(9, DateTimeUnit.HOUR)
        val end = begin.plus(6, DateTimeUnit.HOUR)
        val dutyGuid = "demo-duty-training-$offset"
        val attendees = colleagues.shuffled(rnd).take(2)

        val slots = attendees.mapIndexed { index, employee ->
            Slot(
                guid = "$dutyGuid-attendee-$index",
                employeeGuid = employee.guid,
                requirement = Requirement(RequirementMapping.TRAINING.value),
                begin = begin.toTimestamp(),
                end = end.toTimestamp(),
                inlineEmployee = employee
            )
        } + Slot(
            guid = "$dutyGuid-attendee-self",
            employeeGuid = if (assignSelf) selfEmployee.guid else null,
            requirement = Requirement(RequirementMapping.TRAINING.value),
            begin = begin.toTimestamp(),
            end = end.toTimestamp(),
            inlineEmployee = if (assignSelf) selfEmployee else null
        )

        val definition = DutyDefinition(
            guid = dutyGuid,
            begin = begin.toTimestamp(),
            end = end.toTimestamp(),
            slots = slots
        )

        return GeneratedDuty(definition, DutyType.TRAINING, "[ Schulung ]")
    }

    private fun buildMeetingDuty(date: LocalDate, offset: Int, rnd: Random, assignSelf: Boolean): GeneratedDuty {
        val begin = date.atStartOfDayInstant().plus(18, DateTimeUnit.HOUR)
        val end = begin.plus(2, DateTimeUnit.HOUR)
        val dutyGuid = "demo-duty-meeting-$offset"
        val attendees = colleagues.shuffled(rnd).take(3)

        val slots = attendees.mapIndexed { index, employee ->
            Slot(
                guid = "$dutyGuid-attendee-$index",
                employeeGuid = employee.guid,
                requirement = Requirement(RequirementMapping.TIMESLOT.value),
                begin = begin.toTimestamp(),
                end = end.toTimestamp(),
                inlineEmployee = employee
            )
        } + Slot(
            guid = "$dutyGuid-attendee-self",
            employeeGuid = if (assignSelf) selfEmployee.guid else null,
            requirement = Requirement(RequirementMapping.TIMESLOT.value),
            begin = begin.toTimestamp(),
            end = end.toTimestamp(),
            inlineEmployee = if (assignSelf) selfEmployee else null
        )

        val definition = DutyDefinition(
            guid = dutyGuid,
            begin = begin.toTimestamp(),
            end = end.toTimestamp(),
            slots = slots
        )

        return GeneratedDuty(definition, DutyType.MEET, "[ Besprechung ]")
    }

    private suspend fun seedStorage() {
        StorageService.SELF.update { selfEmployee }
        StorageService.ORG_ITEMS.update { OrgItems(orgs = mapOf(org.guid to org, secondOrg.guid to secondOrg)) }
        StorageService.USER_PREFERENCES.update {
            it.copy(
                allowedOrgs = listOf(org.guid, secondOrg.guid),
                lastSelectedOrg = org.guid
            )
        }
        StorageService.EMPLOYEES.update { EmployeeItems(employees = employeesByGuid) }

        val upcoming = loadUpcoming()
        StorageService.UPCOMING_DUTIES.update { UpcomingDutyItems(minimalDutyDefinitions = upcoming) }

        val currentYear = Clock.System.now().toLocalDateTime(zone).year.toString()
        val minutesServed = loadPast(currentYear).sumOf { it.duration }
        StorageService.STATISTICS.update { Statistics(minutesServed = minutesServed) }
    }
}
