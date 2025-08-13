package me.emiliomini.dutyschedule.services.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.emiliomini.dutyschedule.services.models.NetworkTarget
import me.emiliomini.dutyschedule.data.models.Incode
import me.emiliomini.dutyschedule.data.models.mapping.OrgUnitDataGuid
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.GzipSource
import okio.buffer
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object NetworkService {
    private val TAG = "NetworkService";
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }
    private val jar: CookieJar = JavaNetCookieJar(cookieManager);
    private val httpClient = OkHttpClient.Builder()
        .cookieJar(jar)
        .build();

    suspend fun login(username: String, password: String): Result<String?> {
        return send(
            Request.Builder()
                .url(NetworkTarget.LOGIN.url)
                .post(
                    FormBody.Builder()
                        .add("client", "RKOOE")
                        .add("login", username)
                        .add("password", password)
                        .add("remember", "1")
                        .build()
                )
                .build()
        );
    }

    suspend fun keepAlive(): Result<String?> {
        return send(
            Request.Builder()
                .url(NetworkTarget.KEEP_ALIVE.url)
                .build()
        );
    }

    suspend fun loadPlan(
        incode: Incode,
        orgUnitDataGuid: OrgUnitDataGuid,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<String?> {
        return send(
            Request.Builder()
                .url(NetworkTarget.LOAD_PLAN.url)
                .addHeader(incode.token, incode.value)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept-Encoding", "gzip")
                .post(
                    FormBody.Builder()
                        .add("orgUnitDataGuid", orgUnitDataGuid.value)
                        .add("dateFrom", from.format(DATE_FORMATTER))
                        .add("dateTo", to.format(DATE_FORMATTER))
                        .add("withSubOrgUnits", "1")
                        .add("sortPlan", "false")
                        .build()
                )
                .build()
        );
    }

    suspend fun getStaff(
        incode: Incode,
        orgUnitDataGuid: OrgUnitDataGuid,
        staffDataGuid: List<String>,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<String?> {
        val form = FormBody.Builder()
            .add("orgUnitDataGuid", orgUnitDataGuid.value)
            .add("dateFrom", from.format(DATE_FORMATTER))
            .add("dateTo", to.format(DATE_FORMATTER))
            .add("withSubOrgUnits", "1")
            .add("loadModelData", "1");

        for (guid in staffDataGuid) {
            form.addEncoded("staffDataGuid[]", guid);
        }

        return send(
            Request.Builder()
                .url(NetworkTarget.GET_STAFF.url)
                .addHeader(incode.token, incode.value)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept-Encoding", "gzip")
                .post(form.build())
                .build()
        );
    }

    private suspend fun send(request: Request): Result<String?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = httpClient.newCall(request).execute();
                val responseBody = response.body;
                if (response.isSuccessful && responseBody != null) {
                    if (request.header("Accept-Encoding") == "gzip") {
                        Log.d(TAG, "Trying to decode GZIP response...");
                        val inputStream = GzipSource(responseBody.source()).buffer();
                        Result.success(inputStream.readUtf8());
                    } else {
                        Result.success(response.body?.string());
                    }
                } else {
                    Result.failure(IOException("HTTP error ${response.code} - ${response.body?.string()}"))
                }
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
    }
}

/*suspend fun loadDuties(from: Date, to: Date): Result<List<DutyDefinition>> {
    if (incodeToken == null || incodeValue == null) {
        return Result.failure(IOException("No incode token found!"));
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val dateFromString = dateFormat.format(from)
    val dateToString = dateFormat.format(to)

    val formBodyBuilder = FormBody.Builder()
        .add("orgUnitDataGuid", OrgUnitDataGuid.SATTLEDT.value)
        .add("withSubOrgUnits", "1")
        .add("dateFrom", dateFromString)
        .add("dateTo", dateToString)
        .add("sortPlan", "true")


    val requestBuilder = Request.Builder()
        .addHeader(incodeToken!!, incodeValue!!)
        .addHeader("Content-Type", "application/x-www-form-urlencoded")
        .addHeader("Accept-Encoding", "gzip")
        .url("https://dienstplan.o.roteskreuz.at/StaffPortal/plan/data/loadPlan.json")
        .post(formBodyBuilder.build())

    val request = requestBuilder.build()

    return withContext(Dispatchers.IO) {
        try {
            val response = httpClient.newCall(request).execute()
            val responseBodyString = response.body?.string()

            Log.d(TAG, "LoadDuties Response Code: ${response.code}")
            Log.d(TAG, "LoadDuties Response Body: $responseBodyString")

            if (response.isSuccessful && responseBodyString != null) {
                try {
                    val dutiesJson = JSONObject(responseBodyString)
                    val data = dutiesJson.getJSONObject("data")
                    val keys = data.keys()
                    val result =
                        mutableMapOf<String, DutyDefinition>() // Using start time as key for aggregation

                    for (key in keys) { // 'key' here is the ID from the 'data' object, e.g., "DPP12345"
                        val obj = data.getJSONObject(key)
                        val start = obj.getString("begin")
                        val end = obj.getString("end")

                        // Use 'key' as the DutyDefinition ID, but aggregate by 'start' time
                        var duty = result[start]
                        if (duty == null) {
                            duty = DutyDefinition(
                                key, // Use the actual key from JSON as the Duty ID
                                start,
                                end
                            )
                        }

                        val requirementGroupChildDataGuid =
                            obj.getString("requirementGroupChildDataGuid")
                        val additionalInfos = obj.getJSONObject("additionalInfos")
                        val employeeName = additionalInfos.getString("ressource_name")

                        val employeeId = UUID.randomUUID().toString()
                        when (requirementGroupChildDataGuid) {
                            RequirementGroupChildDataGuid.SEW.value -> {
                                duty.sew = Employee(
                                    employeeId,
                                    employeeName,
                                    "SEW" // Specific identifier for SEW type
                                )
                            }

                            RequirementGroupChildDataGuid.EL.value -> {
                                duty.el = Employee(
                                    employeeId,
                                    employeeName,
                                    "0000000" // Placeholder identifier for EL
                                )
                            }

                            RequirementGroupChildDataGuid.TF.value -> {
                                duty.tf = Employee(
                                    employeeId,
                                    employeeName,
                                    "0000000" // Placeholder identifier for TF
                                )
                            }

                            RequirementGroupChildDataGuid.RS.value -> {
                                duty.rs = Employee(
                                    employeeId,
                                    employeeName,
                                    "0000000" // Placeholder identifier for RS
                                )
                            }
                        }
                        result[start] = duty // Store/update duty aggregated by start time
                    }
                    Result.success(result.values.toList())
                } catch (e: Exception) {
                    Log.e(TAG, "JSON Parsing error: ${e.message}", e)
                    Result.failure(IOException("Failed to parse duties response: ${e.message}", e))
                }
            } else {
                Log.e(
                    TAG,
                    "Failed to load duties: HTTP error ${response.code} - $responseBodyString"
                )
                Result.failure(IOException("Failed to load duties: HTTP error ${response.code}"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException during loadDuties: ${e.message}", e)
            Result.failure(e)
        }
    }
}*/

