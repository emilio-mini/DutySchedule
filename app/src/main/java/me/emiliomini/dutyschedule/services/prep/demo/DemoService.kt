package me.emiliomini.dutyschedule.services.prep.demo

import me.emiliomini.dutyschedule.datastore.prep.IncodeProto
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.duty.MinimalDutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgDayProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.models.network.CreateDutyResponse
import me.emiliomini.dutyschedule.models.prep.Message
import me.emiliomini.dutyschedule.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.services.prep.ScheduleService
import me.emiliomini.dutyschedule.services.prep.live.PrepService
import me.emiliomini.dutyschedule.services.storage.DataKeys
import me.emiliomini.dutyschedule.services.storage.DataStores
import me.emiliomini.dutyschedule.services.storage.StorageService
import java.time.OffsetDateTime

object DemoService : ScheduleService {
    override var isLoggedIn: Boolean = true
    override var self: EmployeeProto? = DemoData.self

    override fun getMessages(): Map<String, List<Message>> {
        return emptyMap()
    }

    override fun getIncode(): IncodeProto? {
        return null
    }

    override suspend fun getOrg(abbreviationOrIdentifier: String): OrgProto? {
        var result = DemoData.orgs.firstOrNull { it.abbreviation == abbreviationOrIdentifier }
        if (result == null) {
            result = DemoData.orgs.firstOrNull { it.identifier == abbreviationOrIdentifier }
        }
        return result
    }

    override suspend fun login(username: String, password: String): Boolean {
        loadSelf("", "")
        loadOrgs()
        loadAllowedOrgs()
        return true
    }

    override suspend fun previouslyLoggedIn(): Boolean {
        return login("", "")
    }

    override suspend fun restoreLogin(): Boolean {
        return login("", "")
    }

    override suspend fun logout() {
        DataStores.clearAndReset()
        DutyScheduleService = PrepService
    }

    override suspend fun loadSelf(
        guid: String?,
        org: String?
    ): EmployeeProto? {
        DataStores.SELF.updateData {
            DemoData.self
        }
        DataStores.STATISTICS.updateData {
            DemoData.statistics
        }
        return DemoData.self
    }

    override suspend fun loadOrgs(): OrgItemsProto? {
        DataStores.ORG_ITEMS.updateData {
            DemoData.orgItems
        }
        return DemoData.orgItems
    }

    override suspend fun loadAllowedOrgs(): List<String>? {
        StorageService.save(
            DataKeys.ALLOWED_ORGS,
            DemoData.allowedOrgs.joinToString(StorageService.DEFAULT_SEPARATOR)
        )
        return DemoData.allowedOrgs
    }

    override suspend fun loadPlan(
        orgUnitDataGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<DutyDefinitionProto>> {
        getStaff(orgUnitDataGuid, emptyList(), from, to)
        return Result.success(emptyList())
    }

    override suspend fun getStaff(
        orgUnitDataGuid: String,
        staffDataGuid: List<String>,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<EmployeeProto>> {
        DataStores.EMPLOYEES.updateData {
            DemoData.employeeItems
        }
        return Result.success(DemoData.employees.filter { staffDataGuid.contains(it.guid) })
    }

    override suspend fun loadTimeline(
        orgUnitDataGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<OrgDayProto>> {
        loadPlan(orgUnitDataGuid, from, to)
        return Result.success(listOf(DemoData.orgDay))
    }

    override suspend fun loadPast(year: String): Result<List<MinimalDutyDefinitionProto>> {
        return Result.success(emptyList())
    }

    override suspend fun loadHoursOfService(year: String): Float {
        return DemoData.statistics.minutesServed / 60f
    }

    override suspend fun loadUpcoming(): List<MinimalDutyDefinitionProto> {
        return DemoData.upcoming
    }

    override suspend fun loadMessages(
        orgUnitDataGuid: String,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<List<Message>> {
        return Result.success(emptyList())
    }

    override suspend fun createAndAllocateDuty(planDataGuid: String): Result<CreateDutyResponse> {
        return Result.success(CreateDutyResponse(true, emptyList(), null, null, "", null))
    }
}