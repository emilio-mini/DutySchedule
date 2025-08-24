package me.emiliomini.dutyschedule.services.api

import android.util.Log
import me.emiliomini.dutyschedule.models.prep.AssignedEmployee
import me.emiliomini.dutyschedule.models.prep.DutyDefinition
import me.emiliomini.dutyschedule.models.prep.Employee
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.models.prep.Type
import me.emiliomini.dutyschedule.models.github.GithubRelease
import org.json.JSONArray
import org.json.JSONObject
import java.time.OffsetDateTime

object DataParserService {
    private const val TAG = "DataParserService"
    private const val DATA_ROOT_POSITION = "data"

    fun parseGithubReleases(root: JSONArray): List<GithubRelease> {
        val releases = mutableListOf<GithubRelease>()

        for (i in 0 until root.length()) {
            val obj = root.getJSONObject(i)

            var downloadUrl = obj.getString("html_url")
            val assets = obj.getJSONArray("assets")
            for (j in 0 until assets.length()) {
                val asset = assets.getJSONObject(j)
                val url = asset.getString("browser_download_url")

                if (url.endsWith(".apk")) {
                    downloadUrl = asset.getString("url")
                    break
                }
            }

            releases.add(
                GithubRelease(
                    obj.getString("tag_name"),
                    obj.getBoolean("prerelease"),
                    OffsetDateTime.parse(obj.getString("published_at")),
                    OffsetDateTime.parse(obj.getString("updated_at")),
                    obj.getString("body"),
                    downloadUrl
                )
            )
        }

        return releases
    }

    fun parseLoadPlan(root: JSONObject): List<DutyDefinition> {
        val data = root.getJSONObject(this.DATA_ROOT_POSITION)
        val duties = mutableMapOf<String, DutyDefinition>()

        // Establish shifts
        for (key in data.keys()) {
            val obj = data.getJSONObject(key)
            val type = obj.getInt(Type.Companion.POSITION)
            if (type != Type.TIMESLOT.value) {
                continue
            }

            val guid = obj.getString("dataGuid")
            val beginTimestamp = obj.getString(DutyDefinition.Companion.BEGIN_POSITION)
            val endTimestamp = obj.getString(DutyDefinition.Companion.END_POSITION)
            val begin = OffsetDateTime.parse(beginTimestamp)
            val end = OffsetDateTime.parse(endTimestamp)

            duties[guid] = DutyDefinition(
                guid,
                begin,
                end,
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                mutableListOf()
            )
        }
        Log.d(TAG, "Established ${duties.size} shifts")

        // Assign staff
        for (key in data.keys()) {
            val obj = data.getJSONObject(key)
            val type = obj.getInt(Type.Companion.POSITION)
            if (type == Type.TIMESLOT.value) {
                continue
            }

            val employeeGuid = obj.getString(Employee.Companion.GUID_POSITION)
            if (employeeGuid.isBlank()) {
                continue
            }

            val guid = obj.getString("parentDataGuid")
            val beginTimestamp = obj.getString(DutyDefinition.Companion.BEGIN_POSITION)
            val endTimestamp = obj.getString(DutyDefinition.Companion.END_POSITION)
            val begin = OffsetDateTime.parse(beginTimestamp)
            val end = OffsetDateTime.parse(endTimestamp)
            val requirement = obj.getString(Requirement.Companion.POSITION)
            val info = obj.getString(Employee.Companion.INFO_POSITION)
            var employeeName = obj.getJSONObject(Employee.Companion.ADDITIONAL_INFOS_POSITION)
                .getString(Employee.Companion.ADDITIONAL_INFOS_NAME_POSITION)
            if (employeeName.isBlank() || employeeName == "Verplant") {
                // Using INFO Tag as fallback
                employeeName = info
            }

            val employee = Employee(
                employeeGuid,
                employeeName,
                if (requirement == Requirement.SEW.value) Employee.Companion.SEW_NAME else null
            )

            val assignedEmployee = AssignedEmployee(
                employee,
                Requirement.Companion.parse(requirement),
                begin,
                end,
                info
            )

            if (duties[guid] == null || duties[guid]?.el == null) {
                Log.e(TAG, "No duties found for $guid")
            }

            when (requirement) {
                Requirement.SEW.value -> {
                    duties[guid]?.sew?.add(assignedEmployee)
                }
                Requirement.EL.value -> {
                    duties[guid]?.el?.add(assignedEmployee)
                }
                Requirement.TF.value -> {
                    duties[guid]?.tf?.add(assignedEmployee)
                }
                Requirement.RS.value -> {
                    duties[guid]?.rs?.add(assignedEmployee)
                }
                else -> {
                    continue
                }
            }
        }

        val sortedDuties = duties.values.sortedBy { it.begin }
        Log.d(TAG, "Parsed ${sortedDuties.size} duties")

        return sortedDuties
    }

    fun parseGetStaff(root: JSONObject): List<Employee> {
        val data = root.getJSONArray(this.DATA_ROOT_POSITION)
        val employees = mutableListOf<Employee>()

        for (i in 0 until data.length()) {
            val obj = data.getJSONObject(i)

            val birthdateTimestamp = obj.getString("birthdate")
            val birthdate = if (birthdateTimestamp.isNotBlank()) OffsetDateTime.parse(birthdateTimestamp) else null

            employees.add(
                Employee(
                    obj.getString(Employee.Companion.STAFF_GUID_POSITION),
                    obj.getString(Employee.Companion.STAFF_NAME_POSITION),
                    this.removeLeadingZeros(obj.getString(Employee.Companion.STAFF_IDENTIFIER_POSITION)),
                    obj.getString("telefon"),
                    obj.getString("email"),
                    obj.getString("externalIsRegularOrgUnit"),
                    birthdate
                )
            )
        }

        return employees
    }

    private fun removeLeadingZeros(input: String): String {
        val regex = "^0+".toRegex()
        return input.replace(regex, "")
    }
}