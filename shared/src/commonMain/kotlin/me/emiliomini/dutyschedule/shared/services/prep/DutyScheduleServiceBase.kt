package me.emiliomini.dutyschedule.shared.services.prep

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
import me.emiliomini.dutyschedule.shared.services.prep.live.PrepService
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


var DutyScheduleService: DutyScheduleServiceBase = PrepService

@OptIn(ExperimentalTime::class)
interface DutyScheduleServiceBase {
    var isLoggedIn: Boolean
    var self: Employee?

    fun getMessages(): Map<String, List<Message>>
    fun getIncode(): Incode?
    suspend fun getOrg(abbreviationOrIdentifier: String): Org?
    suspend fun login(username: String, password: String): Boolean
    suspend fun previouslyLoggedIn(): Boolean
    suspend fun restoreLogin(): Boolean
    suspend fun logout()
    suspend fun loadSelf(guid: String?, org: String?): Employee?
    suspend fun loadOrgs(): OrgItems?
    suspend fun loadAllowedOrgs(): List<String>?
    suspend fun loadPlan(orgUnitDataGuid: String, from: Instant, to: Instant): Pair<List<DutyDefinition>, Map<String, DutyGroup>>
    suspend fun getStaff(orgUnitDataGuid: String, staffDataGuid: List<String>, from: Instant, to: Instant): List<Employee>
    suspend fun loadTimeline(orgUnitDataGuid: String, from: Instant, to: Instant): List<OrgDay>
    suspend fun loadPast(year: String): List<MinimalDutyDefinition>
    suspend fun loadHoursOfService(year: String): Float
    suspend fun loadUpcoming(): List<MinimalDutyDefinition>
    suspend fun loadMessages(orgUnitDataGuid: String, from: Instant, to: Instant): List<Message>
    suspend fun createAndAllocateDuty(planDataGuid: String): CreateDutyResponse?
}
