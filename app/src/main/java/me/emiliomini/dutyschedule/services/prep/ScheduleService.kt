package me.emiliomini.dutyschedule.services.prep

import me.emiliomini.dutyschedule.datastore.prep.IncodeProto
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyGroupProto
import me.emiliomini.dutyschedule.datastore.prep.duty.MinimalDutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgDayProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.models.network.CreateDutyResponse
import me.emiliomini.dutyschedule.models.prep.Message
import me.emiliomini.dutyschedule.services.prep.live.PrepService
import java.time.OffsetDateTime

var DutyScheduleService: ScheduleService = PrepService

interface ScheduleService {
    var isLoggedIn: Boolean
    var self: EmployeeProto?

    fun getMessages(): Map<String, List<Message>>
    fun getIncode(): IncodeProto?
    suspend fun getOrg(abbreviationOrIdentifier: String): OrgProto?
    suspend fun login(username: String, password: String): Boolean
    suspend fun previouslyLoggedIn(): Boolean
    suspend fun restoreLogin(): Boolean
    suspend fun logout()
    suspend fun loadSelf(guid: String?, org: String?): EmployeeProto?
    suspend fun loadOrgs(): OrgItemsProto?
    suspend fun loadAllowedOrgs(): List<String>?
    suspend fun loadPlan(orgUnitDataGuid: String, from: OffsetDateTime, to: OffsetDateTime): Result<Pair<List<DutyDefinitionProto>, Map<String, DutyGroupProto>>>
    suspend fun getStaff(orgUnitDataGuid: String, staffDataGuid: List<String>, from: OffsetDateTime, to: OffsetDateTime): Result<List<EmployeeProto>>
    suspend fun loadTimeline(orgUnitDataGuid: String, from: OffsetDateTime, to: OffsetDateTime): Result<List<OrgDayProto>>
    suspend fun loadPast(year: String): Result<List<MinimalDutyDefinitionProto>>
    suspend fun loadHoursOfService(year: String): Float
    suspend fun loadUpcoming(): List<MinimalDutyDefinitionProto>
    suspend fun loadMessages(orgUnitDataGuid: String, from: OffsetDateTime, to: OffsetDateTime): Result<List<Message>>
    suspend fun createAndAllocateDuty(planDataGuid: String): Result<CreateDutyResponse>
}