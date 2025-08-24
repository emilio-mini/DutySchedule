package me.emiliomini.dutyschedule.services.network

import android.util.Log
import kotlinx.coroutines.delay
import me.emiliomini.dutyschedule.debug.DebugFlags.AVOID_PREP_API
import me.emiliomini.dutyschedule.models.prep.DutyDefinition
import me.emiliomini.dutyschedule.models.prep.Employee
import me.emiliomini.dutyschedule.models.prep.Incode
import me.emiliomini.dutyschedule.models.prep.OrgUnitDataGuid
import me.emiliomini.dutyschedule.models.prep.TimelineItem
import me.emiliomini.dutyschedule.services.storage.DataKeys
import me.emiliomini.dutyschedule.services.storage.StorageService
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.time.OffsetDateTime
import java.util.regex.Pattern

object PrepService {
    private const val TAG = "PrepService"

    private var incode: Incode? = null
    private var self: Employee? = null

    fun getSelf(): Employee? {
        return this.self
    }

    fun getIncode(): Incode? {
        return this.incode
    }

    fun isLoggedIn(): Boolean {
        return this.incode != null
    }


    suspend fun login(username: String, password: String): Boolean {
        var loginResult: Result<String?>
        if (AVOID_PREP_API) {
            loginResult =
                Result.success("...{ headers: { 'x-incode-EXAMPLEKEY': 'EXAMPLEVALUE' } }...")
        } else {
            loginResult = NetworkService.login(username, password)
        }

        val responseBody = loginResult.getOrNull()
        if (responseBody.isNullOrBlank()) {
            return false
        }

        val incodeRegex = Pattern.compile("'(x-incode-[^']+)': '([^']+)'")
        val incodeMatcher = incodeRegex.matcher(responseBody)

        if (incodeMatcher.find()) {
            val incodeToken = incodeMatcher.group(1)
            val incodeValue = incodeMatcher.group(2)
            if (incodeToken.isNullOrBlank() || incodeValue.isNullOrBlank()) {
                return false
            }

            this.incode = Incode(incodeToken, incodeValue)

            StorageService.save(DataKeys.USERNAME, username)
            StorageService.save(DataKeys.PASSWORD, password)

            Log.d(TAG, "Logged in!")
        } else {
            Log.w(TAG, "Could not find x-incode in response body")
            return false
        }

        val guidRegex = Pattern.compile("ressourceDataGuid === '([^']+)'")
        val guidMatcher = guidRegex.matcher(responseBody)
        if (guidMatcher.find()) {
            val guid = guidMatcher.group(1)
            if (guid.isNullOrBlank()) {
                return false
            }

            val staffResult = this.getStaff(
                OrgUnitDataGuid.EMS_SATTLEDT,
                listOf(guid),
                OffsetDateTime.now(),
                OffsetDateTime.now()
            ).getOrNull()
            if (staffResult != null && staffResult.isNotEmpty()) {
                this.self = staffResult.firstOrNull { it.guid == guid }
            }

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

        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            return false
        }

        if (AVOID_PREP_API) {
            return this.login(username, password)
        }

        // Try restoring using keepalive
        val keepAlive = NetworkService.keepAlive().getOrNull()
        if (keepAlive == "true") {
            return true
        }

        return this.login(username, password)
    }

    suspend fun logout() {
        this.incode = null
        StorageService.clear(DataKeys.USERNAME)
        StorageService.clear(DataKeys.PASSWORD)
    }

    suspend fun loadPlan(
        orgUnitDataGuid: OrgUnitDataGuid,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<DutyDefinition>> {
        Log.d(TAG, "Loading plan...")

        if (!this.isLoggedIn()) {
            return Result.failure(IOException("Not logged in!"))
        }

        if (AVOID_PREP_API) {
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
        orgUnitDataGuid: OrgUnitDataGuid,
        staffDataGuid: List<String>,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<Employee>> {
        Log.d(TAG, "Getting staff...")

        if (!this.isLoggedIn()) {
            return Result.failure(IOException("Not logged in!"))
        }

        if (AVOID_PREP_API) {
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
        orgUnitDataGuid: OrgUnitDataGuid,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<TimelineItem>> {
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

        // Note: This is trash
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

        val sortedPlan = plan.sortedBy { it.begin }
        val timelineItems = mutableListOf<TimelineItem>()

        var previousBegin: OffsetDateTime? = null
        for (duty in sortedPlan) {
            if (previousBegin == null || previousBegin.toLocalDate() != duty.begin.toLocalDate()) {
                timelineItems.add(TimelineItem.Date(duty.begin))
            }

            timelineItems.add(TimelineItem.Duty(duty))
            previousBegin = duty.begin
        }

        Log.d(TAG, "Loaded ${timelineItems.size} timeline elements")
        return Result.success(timelineItems.toList())
    }

}

