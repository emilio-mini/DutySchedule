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
import okhttp3.Headers.Companion.headersOf
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.java.net.cookiejar.JavaNetCookieJar
import okhttp3.logging.HttpLoggingInterceptor
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
    private const val TAG = "NetworkService"
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }
    private val jar: CookieJar = JavaNetCookieJar(cookieManager)
    private val interceptor =
        HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .cookieJar(jar)
        .build()

    suspend fun downloadFileWithProgress(
        context: Context,
        urlString: String,
        headers: Headers,
        fileName: String,
        onProgress: (Int) -> Unit
    ): File? {
        return withContext(Dispatchers.IO) {
            val request = Request(
                url = urlString.toHttpUrl(),
                headers = headers
            )

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
                response.body.close()
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
        Log.d(TAG, "Trying to get latest version")

        val request = Request(
            url = NetworkTarget.LATEST_RELEASE.httpUrl(),
            headers = headersOf(
                "Authorization", "Bearer ${BuildConfig.GITHUB_API_TOKEN}"
            )
        )
        val latestBody = send(request).getOrNull()

        if (latestBody == null) {
            return Result.failure(IOException("Failed to get latest version!"))
        }

        val releases = DataParserService.parseGithubReleases(JSONArray(latestBody))
        val latest = releases.maxByOrNull { it.publishedAt }

        return Result.success(latest)
    }

    suspend fun login(username: String, password: String): Result<String?> {
        val form = FormBody.Builder()
            .add("client", "RKOOE")
            .add("login", username)
            .add("password", password)
            .add("remember", "1")

        val request = Request(
            url = NetworkTarget.LOGIN.httpUrl(),
            body = form.build()
        )
        return send(request)
    }

    suspend fun keepAlive(): Result<String?> {
        val request = Request(
            url = NetworkTarget.KEEP_ALIVE.httpUrl(),
        )
        return send(request)
    }

    suspend fun loadPlan(
        incode: Incode,
        orgUnitDataGuid: OrgUnitDataGuid,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): Result<String?> {
        val form = FormBody.Builder()
            .add("orgUnitDataGuid", orgUnitDataGuid.value)
            .add("dateFrom", from.format(DATE_FORMATTER))
            .add("dateTo", to.format(DATE_FORMATTER))
            .add("withSubOrgUnits", "1")
            .add("sortPlan", "false")

        val request = Request(
            url = NetworkTarget.LOAD_PLAN.httpUrl(),
            headers = headersOf(
                incode.token, incode.value,
                "Content-Type", "application/x-www-form-urlencoded",
                "Accept-Encoding", "gzip"
            ),
            body = form.build()
        )
        return send(request)
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
            .add("loadModelData", "1")

        for (guid in staffDataGuid) {
            form.addEncoded("staffDataGuid[]", guid)
        }

        val request = Request(
            url = NetworkTarget.GET_STAFF.httpUrl(),
            headers = headersOf(
                incode.token, incode.value,
                "Content-Type", "application/x-www-form-urlencoded",
                "Accept-Encoding", "gzip"
            ),
            body = form.build()
        )
        return send(request)
    }

    private suspend fun send(request: Request): Result<String?> {
        return withContext(Dispatchers.IO) {

            httpClient.newCall(request).execute().use { response ->
                if (request.header("Accept-Encoding") == "gzip") {
                    Log.d(TAG, "Trying to decode GZIP response...")
                    val inputStream = GzipSource(response.body.source()).buffer()
                    val result = inputStream.readUtf8()
                    response.body.close()
                    Result.success(result)
                } else {
                    val result = response.body.string()
                    response.body.close()
                    Result.success(result)
                }
            }
        }
    }
}
