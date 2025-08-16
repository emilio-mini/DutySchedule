package me.emiliomini.dutyschedule.services.api

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.emiliomini.dutyschedule.BuildConfig
import me.emiliomini.dutyschedule.data.models.Incode
import me.emiliomini.dutyschedule.data.models.mapping.OrgUnitDataGuid
import me.emiliomini.dutyschedule.data.models.vc.GithubRelease
import me.emiliomini.dutyschedule.services.models.NetworkTarget
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.GzipSource
import okio.buffer
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
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

    suspend fun downloadFileWithProgress(
        context: Context,
        urlString: String,
        headers: Headers,
        fileName: String,
        onProgress: (Int) -> Unit
    ): File? {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(urlString).headers(headers).build()

            try {
                val response = httpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e(TAG, "Server returned an error: ${response.code}")
                    response.body.close()
                    onProgress(0)
                    return@withContext null
                }

                val body = response.body
                val contentLength = body.contentLength()
                val file = File(context.filesDir, fileName)
                val outputStream = FileOutputStream(file)

                outputStream.use { output ->
                    body.byteStream().use { input ->
                        var bytesCopied = 0L
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            val progress = ((bytesCopied * 100) / contentLength).toInt()
                            onProgress(progress)
                            bytes = input.read(buffer)
                        }
                    }
                }

                Log.d(TAG, "File downloaded successfully")
                onProgress(100)
                return@withContext file
            } catch (e: IOException) {
                Log.e(TAG, "Error downloading file", e)
                onProgress(0)
                return@withContext null
            }
        }
    }

    suspend fun getLatestVersion(): Result<GithubRelease?> {
        val latestBody = send(
            Request.Builder()
                .url(NetworkTarget.LATEST_RELEASE.url)
                .header("Authorization", "Bearer ${BuildConfig.GITHUB_API_TOKEN}")
                .build()
        ).getOrNull()

        val releases = DataParserService.parseGithubReleases(JSONArray(latestBody))
        val latest = releases.maxByOrNull { it.publishedAt }

        return Result.success(latest)
    }

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
