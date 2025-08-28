package me.emiliomini.dutyschedule.services.network

import android.util.Log
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.models.prep.AssignedEmployee
import me.emiliomini.dutyschedule.models.prep.DutyDefinition
import me.emiliomini.dutyschedule.models.prep.Employee
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.models.prep.Type
import me.emiliomini.dutyschedule.models.github.GithubRelease
import me.emiliomini.dutyschedule.models.prep.DutyType
import me.emiliomini.dutyschedule.models.prep.Message
import me.emiliomini.dutyschedule.models.prep.MinimalDutyDefinition
import me.emiliomini.dutyschedule.models.prep.Resource
import me.emiliomini.dutyschedule.models.prep.Skill
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

    fun parseOrgTree(root: JSONObject): OrgItemsProto? {
        val orgItemsProto = OrgItemsProto.newBuilder()

        for (key in root.keys()) {
            val obj = root.getJSONObject(key)
            val additionalInfos = obj.getJSONObject("additionalInfos")

            val guid = key
            val title = additionalInfos.getString("orgUnitName")
            val abbreviation = additionalInfos.getString("orgUnitAbbreviation")
            val identifier = additionalInfos.getString("externalId")

            orgItemsProto.addOrgs(
                OrgProto.newBuilder()
                    .setGuid(guid)
                    .setTitle(title)
                    .setAbbreviation(abbreviation)
                    .setIdentifier(identifier)
                    .build()
            )
        }

        return orgItemsProto.build()
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
            val skill = obj.getString(Skill.Companion.POSITION)
            var employeeName = obj.getJSONObject(Employee.Companion.ADDITIONAL_INFOS_POSITION)
                .getString(Employee.Companion.ADDITIONAL_INFOS_NAME_POSITION)
            if (employeeName.isBlank() || employeeName == "Verplant") {
                // Using INFO Tag as fallback
                employeeName = info
            }

            //val skills: MutableList<Skill> = mutableListOf()

            /*if (skill != null) {
                for (j in 0 until skill.length()) {
                    val skillObject = skill.getJSONObject(j)
                    val skillDataGuid = skillObject.optString("skillDataGuid")
                    val currentParsedSkill = Skill.Companion.parse(skillDataGuid)

                    if (currentParsedSkill != Skill.INVALID) {
                        /*if (currentParsedSkill == Skill.NFS) {
                            bestSkillSoFar = Skill.NFS
                            break
                        }

                        else if (currentParsedSkill == Skill.RS && bestSkillSoFar != Skill.NFS) {
                            bestSkillSoFar = Skill.RS
                        }

                        else if (currentParsedSkill == Skill.AZUBI && bestSkillSoFar != Skill.NFS && bestSkillSoFar != Skill.RS) {
                            bestSkillSoFar = Skill.AZUBI
                        }

                        else if (bestSkillSoFar == Skill.INVALID) {
                            bestSkillSoFar = currentParsedSkill
                        }*/
                        skills.add(currentParsedSkill)
                    }
                }
            }*/

            val employee = Employee(
                employeeGuid,
                employeeName,
                when (requirement) {
                    Requirement.SEW.value -> Employee.Companion.SEW_NAME
                    Requirement.ITF.value -> Employee.Companion.ITF_NAME
                    Requirement.RTW.value -> Employee.Companion.RTW_NAME
                    Requirement.HAEND.value -> Employee.Companion.HAEND_NAME
                    else -> null
                },
                resourceTypeGuid = obj.getString("ressourceTypeDataGuid"),
                //skill = Skill.Companion.parse(skill),
            )

            val assignedEmployee = AssignedEmployee(
                employee,
                Requirement.Companion.parse(requirement),
                begin,
                end,
                info,
                //skill = Skill.Companion.parse(skill)
            )

            if (duties[guid] == null || duties[guid]?.el == null) {
                Log.e(TAG, "No duties found for $guid")
            }

            when (requirement) {
                Requirement.SEW.value -> {
                    duties[guid]?.sew?.add(assignedEmployee)
                }

                Requirement.RTW.value -> {
                    duties[guid]?.sew?.add(assignedEmployee)
                }

                Requirement.ITF.value -> {
                    duties[guid]?.sew?.add(assignedEmployee)
                }

                Requirement.HAEND.value -> {
                    duties[guid]?.sew?.add(assignedEmployee)
                }

                Requirement.EL.value -> {
                    duties[guid]?.el?.add(assignedEmployee)
                }

                Requirement.RTW_RS.value -> {
                    duties[guid]?.el?.add(assignedEmployee)
                }

                Requirement.TF.value -> {
                    duties[guid]?.tf?.add(assignedEmployee)
                }

                Requirement.RS.value -> {
                    duties[guid]?.rs?.add(assignedEmployee)
                }

                Requirement.HAEND_EL.value -> {
                    duties[guid]?.el?.add(assignedEmployee)
                }

                Requirement.RTW_NFS.value -> {
                    duties[guid]?.tf?.add(assignedEmployee)
                }

                Requirement.ITF_NFS.value -> {
                    duties[guid]?.tf?.add(assignedEmployee)
                }

                Requirement.ITF_LKW.value -> {
                    duties[guid]?.el?.add(assignedEmployee)
                }

                else -> {
                    duties[guid]?.rs?.add(assignedEmployee)
                }
            }
        }

        val sortedDuties = duties.values.sortedBy { it.begin }
        Log.d(TAG, "Parsed ${sortedDuties.size} duties")

        return sortedDuties
    }

    fun parseLoadMinimalDutyDefinitions(root: JSONObject): List<MinimalDutyDefinition> {
        val data = root.getJSONArray(this.DATA_ROOT_POSITION)
        val duties = mutableListOf<MinimalDutyDefinition>()

        for (i in 0 until data.length()) {
            val obj = data.getJSONObject(i)

            val allocationInfo = obj.getJSONObject("allocationInfo")
            val allocationKey = allocationInfo.keys().asSequence().firstOrNull()
            val allocations =
                if (allocationKey != null) allocationInfo.getJSONArray(allocationKey) else null

            val typeString = allocations?.getString(0)
            val type = when (typeString) {
                "[ SEW ]" -> DutyType.EMS
                "[ Schulung ]" -> DutyType.TRAINING
                else -> DutyType.UNKNOWN
            }

            val staffList = mutableListOf<String>()
            if (allocations != null) {
                for (j in 1 until allocations.length()) {
                    staffList.add(allocations.getString(j) ?: "")
                }
            }
            staffList.filter { it.isNotBlank() }
            val vehicle =
                staffList.find { it.contains("SEW") || it.contains("ITF") || it.contains("RTW") }
            staffList.filter { it != vehicle }

            duties.add(
                MinimalDutyDefinition(
                    obj.getString("guid"),
                    OffsetDateTime.parse(obj.getString("begin")),
                    OffsetDateTime.parse(obj.getString("end")),
                    type,
                    vehicle,
                    staffList,
                    obj.getInt("duration")
                )
            )
        }

        return duties
    }

    fun parseGetStaff(root: JSONObject): List<Employee> {
        val data = root.getJSONArray(this.DATA_ROOT_POSITION)
        val employees = mutableListOf<Employee>()

        for (i in 0 until data.length()) {
            val obj = data.getJSONObject(i)

            val birthdateTimestamp = obj.getString("birthdate")
            val birthdate =
                if (birthdateTimestamp.isNotBlank()) OffsetDateTime.parse(birthdateTimestamp) else null

            val staffToSkillsArray = obj.optJSONArray(Employee.Companion.SKILL_STAFF_POSITION)
            val skills: MutableList<Skill> = mutableListOf()

            if (staffToSkillsArray != null) {
                for (j in 0 until staffToSkillsArray.length()) {
                    val skillObject = staffToSkillsArray.getJSONObject(j)
                    val skillDataGuid = skillObject.optString("skillDataGuid")
                    val currentParsedSkill = Skill.Companion.parse(skillDataGuid)

                    if (currentParsedSkill != Skill.INVALID) {
                        if (!skills.contains(currentParsedSkill)) {
                            skills.add(currentParsedSkill)
                        }

                    }
                }
            }

            employees.add(
                Employee(
                    obj.getString(Employee.Companion.STAFF_GUID_POSITION),
                    obj.getString(Employee.Companion.STAFF_NAME_POSITION),
                    this.removeLeadingZeros(obj.getString(Employee.Companion.STAFF_IDENTIFIER_POSITION)),
                    obj.getString("telefon"),
                    obj.getString("email"),
                    obj.getString("externalIsRegularOrgUnit"),
                    birthdate,
                    obj.getString("ressourceTypeDataGuid"),
                    skill = skills
                )
            )
        }

        return employees
    }

    fun parseGetResources(root: JSONObject): List<Resource>? {
        val data = root.getJSONObject(this.DATA_ROOT_POSITION)
        val resources = mutableListOf<Resource>()

        for (key in data.keys()) {
            val obj = data.getJSONObject(key)

            resources.add(
                Resource(
                    obj.getString("ressourceTypeDataGuid"),
                    obj.getString("dataGuid")
                )
            )
        }

        return resources
    }

    fun parseGetMessages(root: JSONObject): List<Message>? {

        try {
            if (!root.has("data")) {
                Log.w(TAG, "parseGetMessages: 'data' field missing in JSON root.")
                return listOf()
            }

            val dataNode =
                root.opt("data") // opt verwenden, um keine Exception bei falschem Typ zu werfen

            if (dataNode == null) { // Sollte durch root.has abgedeckt sein, aber doppelt sicher
                Log.w(TAG, "parseGetMessages: 'data' field is null.")
                return listOf()
            }


            val data = root.getJSONObject(this.DATA_ROOT_POSITION)
            val messages = mutableListOf<Message>()

            for (key in data.keys()) {
                val obj = data.getJSONObject(key)

                messages.add(
                    Message(
                        obj.getString("dataGuid"),
                        obj.getString("typeGuid"),
                        obj.getString("title"),
                        obj.getString("message"),
                        obj.getInt("messagePriority"),
                        OffsetDateTime.parse(obj.getString("displayFrom")),
                        OffsetDateTime.parse(obj.getString("displayTo"))
                    )
                )
            }

            return messages
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON", e)
            return listOf()

        }
    }

    private fun removeLeadingZeros(input: String): String {
        val regex = "^0+".toRegex()
        return input.replace(regex, "")
    }
}