package me.emiliomini.dutyschedule.shared.services.prep

import me.emiliomini.dutyschedule.shared.datastores.CreateDutyResponse
import me.emiliomini.dutyschedule.shared.datastores.DutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.DutyGroup
import me.emiliomini.dutyschedule.shared.datastores.DutyContext
import me.emiliomini.dutyschedule.shared.datastores.DutyLink
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.Incode
import me.emiliomini.dutyschedule.shared.datastores.Message
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Org
import me.emiliomini.dutyschedule.shared.datastores.OrgDay
import me.emiliomini.dutyschedule.shared.datastores.OrgItems
import me.emiliomini.dutyschedule.shared.services.prep.live.PrepService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
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
    /**
     * The nullable load results below are null when the request failed and empty when the server
     * reported nothing; persisting or rendering a failure as empty wipes the cached data it was
     * supposed to refresh
     */
    suspend fun loadPlan(orgUnitDataGuid: String, from: Instant, to: Instant): Pair<List<DutyDefinition>, Map<String, DutyGroup>>?
    suspend fun getStaff(orgUnitDataGuid: String, staffDataGuid: List<String>, from: Instant, to: Instant): List<Employee>

    /**
     * Served from an in memory cache while it is fresh, so moving between screens does not refetch
     * a plan that is already on hand. [forceRefresh] is what a manual pull to refresh passes
     */
    suspend fun loadTimeline(orgUnitDataGuid: String, from: Instant, to: Instant, forceRefresh: Boolean = false): List<OrgDay>?

    /**
     * The cached timeline if one is still fresh, without suspending. Lets a screen render known
     * data on its first frame instead of flashing a spinner while an effect fetches the same thing
     */
    fun peekTimeline(orgUnitDataGuid: String, from: Instant, to: Instant): List<OrgDay>?

    /** The org the schedule opens on: last used, else the user's own, else the first allowed one */
    suspend fun getDefaultOrgGuid(): String?

    /** Warms the timeline cache for the schedule's opening view without blocking the caller */
    fun preloadTimeline()
    /**
     * The plan entry an upcoming duty stands for, if it has been worked out before. Resolving is
     * expensive enough that the result is persisted, so this reads it back without suspending
     */
    fun peekDutyLink(upcomingGuid: String): DutyLink?

    /**
     * Finds the plan entry behind [duty] by searching the plans of every org the user may see, and
     * remembers the pairing. Returns the stored link straight away when there already is one, and
     * null when no plan holds a matching duty. [retryUnresolved] looks again at a duty an earlier
     * pass gave up on, which is what a deliberate tap on it warrants
     */
    suspend fun resolveDutyLink(
        duty: MinimalDutyDefinition,
        retryUnresolved: Boolean = false
    ): DutyLink?

    /** Works through every upcoming duty that has no link yet, without blocking the caller */
    fun resolveUpcomingDutyLinks()

    /**
     * The plan entry behind an upcoming duty together with the vehicle handovers on either side of
     * it. Costs one plan request over the shift plus a margin, so the result is cached briefly
     */
    suspend fun loadDutyContext(duty: MinimalDutyDefinition): DutyContext?

    /**
     * The stored context for an upcoming duty, however old, without suspending. Screens follow
     * [StorageService.DUTY_CONTEXTS] instead, so that they also pick it up when the store finishes
     * loading; this is for callers that only need the value they can see right now
     */
    fun peekDutyContext(upcomingGuid: String): DutyContext?
    suspend fun loadPast(year: String): List<MinimalDutyDefinition>?
    suspend fun loadHoursOfService(year: String): Float?
    suspend fun loadUpcoming(): List<MinimalDutyDefinition>?
    suspend fun loadMessages(orgUnitDataGuid: String, from: Instant, to: Instant): List<Message>
    suspend fun createAndAllocateDuty(planDataGuid: String): CreateDutyResponse?
}
