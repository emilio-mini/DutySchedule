package me.emiliomini.dutyschedule.shared.services.network

import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.http.parameters
import io.ktor.utils.io.InternalAPI
import me.emiliomini.dutyschedule.shared.api.getPlatformLogger
import me.emiliomini.dutyschedule.shared.datastores.Incode
import me.emiliomini.dutyschedule.shared.mappings.docScedConfigFromString
import me.emiliomini.dutyschedule.shared.services.network.MultiplatformNetworkAdapter.HTTP
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.isInvalid
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


object NetworkService {
    private const val UNAUTHORIZED_MESSAGE = "Error 401: Unauthorized API Call"
    private const val TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    private val logger = getPlatformLogger("NetworkService")

    @OptIn(InternalAPI::class)
    suspend fun login(username: String, password: String): HttpResponse {
        val formResponse = HTTP.submitForm(
            url = Endpoints.LOGIN.url,
            formParameters = parameters {
                append("client", "RKOOE")
                append("login", username)
                append("password", password)
                append("remember", "1")
            }
        )

        return if (formResponse.status == HttpStatusCode.Found && formResponse.bodyAsText().isBlank()) {
            HTTP.get(Endpoints.SCHEDULE_BASE.url)
        } else {
            formResponse
        }
    }

    suspend fun keepAlive(): HttpResponse {
        return HTTP.get {
            url(Endpoints.KEEP_ALIVE.url)
        }
    }

    suspend fun dispo(): HttpResponse {
        return HTTP.get {
            url(Endpoints.DISPO.url)
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun loadPlan(
        incode: Incode,
        orgUnitDataGuid: String,
        from: Instant,
        to: Instant
    ): HttpResponse? {
        if (incode.isInvalid()) {
            logger.error("loadPlan: Invalid incode")
            return null
        }

        return HTTP.submitForm(
            url = Endpoints.LOAD_PLAN.url,
            formParameters = parameters {
                append("orgUnitDataGuid", orgUnitDataGuid)
                append("dateFrom", from.format(TIMESTAMP_PATTERN))
                append("dateTo", to.format(TIMESTAMP_PATTERN))
                append("withSubOrgUnits", "1")
                append("sortPlan", "false")
            }
        ) {
            headers {
                append(incode.token, incode.value)
                append("Content-Type", "application/x-www-form-urlencoded")
                append("Accept-Encoding", "gzip")
            }
        }
    }

    suspend fun loadUpcoming(
        incode: Incode
    ): HttpResponse? {
        if (incode.isInvalid()) {
            logger.error("loadUpcoming: Invalid incode")
            return null
        }

        return HTTP.submitForm(
            url = Endpoints.LOAD_UPCOMING.url,
            formParameters = parameters {
                append("year", "")
                append("month", "")
                append("dateDescendingSort", "true")
                append("max", "30")
                append("orgUnit", "")
                append("withSubOrgs", "on")
                append("form.event.onsubmit", "searchForm")
            }
        ) {
            headers {
                append(incode.token, incode.value)
                append("Content-Type", "application/x-www-form-urlencoded")
                append("Accept-Encoding", "gzip")
            }
        }
    }

    suspend fun loadPast(
        incode: Incode,
        year: String
    ): HttpResponse? {
        if (incode.isInvalid()) {
            logger.error("loadPast: Invalid incode")
            return null
        }

        return HTTP.submitForm(
            url = Endpoints.LOAD_PAST.url,
            formParameters = parameters {
                append("year", year)
                append("month", "")
                append("dateDescendingSort", "true")
                append("orgUnit", "")
                append("withSubOrgs", "on")
                append("form.event.onsubmit", "searchForm")
            }
        ) {
            headers {
                append(incode.token, incode.value)
                append("Content-Type", "application/x-www-form-urlencoded")
                append("Accept-Encoding", "gzip")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getMessages(
        incode: Incode,
        orgUnitDataGuid: String,
        from: Instant,
        to: Instant
    ): HttpResponse? {
        if (incode.isInvalid()) {
            logger.error("getMessages: Invalid incode")
            return null
        }

        return HTTP.submitForm(
            url = Endpoints.GET_MESSAGES.url,
            formParameters = parameters {
                append("orgUnitDataGuid", orgUnitDataGuid)
                append("dateFrom", from.format(TIMESTAMP_PATTERN))
                append("dateTo", to.format(TIMESTAMP_PATTERN))
                append("withSubOrgUnits", "1")
                append("timeShiftDate", "")
            }
        ) {
            headers {
                append(incode.token, incode.value)
                append("Content-Type", "application/x-www-form-urlencoded")
                append("Accept-Encoding", "gzip")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getResources(
        incode: Incode,
        orgUnitDataGuid: String,
        from: Instant,
        to: Instant
    ): HttpResponse? {
        if (incode.isInvalid()) {
            logger.error("getResources: Invalid incode")
            return null
        }

        return HTTP.submitForm(
            url = Endpoints.GET_RESOURCES.url,
            formParameters = parameters {
                append("orgUnitDataGuid", orgUnitDataGuid)
                append("dateFrom", from.format(TIMESTAMP_PATTERN))
                append("dateTo", to.format(TIMESTAMP_PATTERN))
                append("withSubOrgUnits", "1")
            }
        ) {
            headers {
                append(incode.token, incode.value)
                append("Content-Type", "application/x-www-form-urlencoded")
                append("Accept-Encoding", "gzip")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getStaff(
        incode: Incode,
        orgUnitDataGuid: String,
        staffDataGuid: List<String>,
        from: Instant,
        to: Instant
    ): HttpResponse? {
        if (incode.isInvalid()) {
            logger.error("loadPast: Invalid incode")
            return null
        }

        return HTTP.submitForm(
            url = Endpoints.GET_STAFF.url,
            formParameters = parameters {
                append("orgUnitDataGuid", orgUnitDataGuid)
                append("dateFrom", from.format(TIMESTAMP_PATTERN))
                append("dateTo", to.format(TIMESTAMP_PATTERN))
                append("withSubOrgUnits", "1")
                append("loadModelData", "1")

                for (guid in staffDataGuid) {
                    append("staffDataGuid[]", guid)
                }
            }
        ) {
            headers {
                append(incode.token, incode.value)
                append("Content-Type", "application/x-www-form-urlencoded")
                append("Accept-Encoding", "gzip")
            }
        }
    }

    suspend fun loadDocScedCalendar(
        orgUnitDataGuid: String,
        gran: String? = null // optional: "wee" | "mon" | "nextMon" | "ges"
    ): HttpResponse {
        return HTTP.get {
            url {
                url(Endpoints.DOCSCED.url)
                parameter("site", "calendar")
                parameter("config", docScedConfigFromString(orgUnitDataGuid))

                if (!gran.isNullOrBlank()) {
                    parameter("gran", gran)
                }
            }
        }
    }

    suspend fun createAndAllocateDuty(
        incode: Incode,
        planDataGuid: String
    ): HttpResponse? {
        if (incode.isInvalid()) {
            logger.error("loadPast: Invalid incode")
            return null
        }

        return HTTP.submitForm(
            url = Endpoints.CREATE_AND_ALLOCATE_DUTY.url,
            formParameters = parameters {
                append("plan", planDataGuid)
            }
        ) {
            headers {
                append(incode.token, incode.value)
                append("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                append("Accept", "application/json, text/javascript, */*; q=0.01")
                append("X-Requested-With", "XMLHttpRequest")
                append("Accept-Encoding", "gzip")
                append("Origin", "https://dienstplan.o.roteskreuz.at")
                append("Referer", "https://dienstplan.o.roteskreuz.at/StaffPortal/dispo.php")
            }
        }
    }
}