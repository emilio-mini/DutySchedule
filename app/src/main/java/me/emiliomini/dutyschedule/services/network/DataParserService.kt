package me.emiliomini.dutyschedule.services.network

import android.util.Log
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyTypeProto
import me.emiliomini.dutyschedule.datastore.prep.duty.MinimalDutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.employee.AssignedEmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.datastore.prep.employee.SkillProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.json.mapping.DutyDefinitionProtoMapping
import me.emiliomini.dutyschedule.json.mapping.MinimalDutyDefinitionProtoMapping
import me.emiliomini.dutyschedule.json.mapping.OrgProtoMapping
import me.emiliomini.dutyschedule.json.mapping.PrepResponseMapping
import me.emiliomini.dutyschedule.json.util.forEach
import me.emiliomini.dutyschedule.json.util.map
import me.emiliomini.dutyschedule.json.util.value
import me.emiliomini.dutyschedule.models.network.CreateDutyResponse
import me.emiliomini.dutyschedule.models.network.CreatedDuty
import me.emiliomini.dutyschedule.models.prep.Employee
import me.emiliomini.dutyschedule.models.prep.Message
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.models.prep.Resource
import me.emiliomini.dutyschedule.models.prep.Type
import me.emiliomini.dutyschedule.util.toEpochMilli
import me.emiliomini.dutyschedule.util.toTimestamp
import me.emiliomini.dutyschedule.util.trimLeadingZeros
import org.json.JSONArray
import org.json.JSONObject
import java.time.OffsetDateTime

object DataParserService {
    private const val TAG = "DataParserService"
    private const val DATA_ROOT_POSITION = "data"

    fun parseOrgTree(root: JSONObject): OrgItemsProto? {
        return OrgItemsProto.newBuilder()
            .putAllOrgs(
                root.map({
                    it.key
                }, {
                    OrgProto.newBuilder()
                        .setGuid(it.key)
                        .setTitle(it.o.value(OrgProtoMapping.TITLE))
                        .setAbbreviation(it.o.value(OrgProtoMapping.ABBREVIATION))
                        .setIdentifier(it.o.value(OrgProtoMapping.IDENTIFIER))
                        .build()
                })
            )
            .build()
    }

    fun parseLoadPlan(root: JSONObject): List<DutyDefinitionProto> {
        val data = root.value(PrepResponseMapping.DATA_AS_OBJECT)
        if (data == null) {
            return emptyList()
        }

        val duties = mutableMapOf<String, DutyDefinitionProto>()
        duties.putAll(
            data.map({
                it.o.value(DutyDefinitionProtoMapping.GUID)
            }, {
                val type = it.o.value(DutyDefinitionProtoMapping.TYPE)
                if (type != Type.TIMESLOT.value) {
                    it.skip()
                    null
                }

                val def = DutyDefinitionProto.newBuilder()
                    .setGuid(it.o.value(DutyDefinitionProtoMapping.GUID))
                    .setBegin(it.o.value(DutyDefinitionProtoMapping.BEGIN))
                    .setEnd(it.o.value(DutyDefinitionProtoMapping.END))

                val info = it.o.value(DutyDefinitionProtoMapping.INFO)!!
                if (info.isNotBlank()) {
                    def.setInfo(info)
                }

                def.build()
            })
        )
        Log.d(TAG, "Established ${duties.size} shifts")

        // Assign staff
        data.forEach {
            val type = it.o.value(DutyDefinitionProtoMapping.TYPE)
            if (type == Type.TIMESLOT.value) {
                return@forEach
            }

            val employeeGuid = it.o.value(DutyDefinitionProtoMapping.EMPLOYEE_GUID)!!
            val parentGuid = it.o.value(DutyDefinitionProtoMapping.PARENT_GUID)!!
            val guid = it.o.value(DutyDefinitionProtoMapping.GUID)!!
            var name = it.o.value(DutyDefinitionProtoMapping.INLINE_NAME)!!
            val info = it.o.value(DutyDefinitionProtoMapping.INFO)!!
            val requirement = it.o.value(DutyDefinitionProtoMapping.REQUIREMENT)!!

            if (employeeGuid.isBlank()) {
                when (requirement) {
                    // Staff
                    Requirement.EL.value,
                    Requirement.RTW_RS.value,
                    Requirement.HAEND_EL.value,
                    Requirement.ITF_LKW.value-> {
                        duties[parentGuid] =
                            duties[parentGuid]?.toBuilder()
                                ?.setElSlotId(guid)
                                ?.build() ?: return@forEach
                    }

                    Requirement.TRAINING.value,
                    Requirement.TF.value,
                    Requirement.ITF_NFS.value,
                    Requirement.RTW_NFS.value-> {
                        duties[parentGuid] =
                            duties[parentGuid]?.toBuilder()
                                ?.setTfSlotId(guid)
                                ?.build() ?: return@forEach
                    }

                    // Requirement.RS.value
                    else -> {
                        duties[parentGuid] =
                            duties[parentGuid]?.toBuilder()
                                ?.setRsSlotId(guid)
                                ?.build() ?: return@forEach
                    }
                }

                return@forEach
            }

            if (name.isBlank() || name == "Verplant") {
                // Using INFO Tag as fallback
                name = info
            }
            val employee = EmployeeProto.newBuilder()
                .setGuid(employeeGuid)
                .setName(name)
                .setIdentifier(
                    when (requirement) {
                        Requirement.VEHICLE.value -> Employee.Companion.VEHICLE_NAME
                        Requirement.SEW.value -> Employee.Companion.SEW_NAME
                        Requirement.ITF.value -> Employee.Companion.ITF_NAME
                        Requirement.RTW.value -> Employee.Companion.RTW_NAME
                        Requirement.HAEND.value -> Employee.Companion.HAEND_NAME
                        else -> ""
                    }
                )
                .setResourceTypeGuid(it.o.value(DutyDefinitionProtoMapping.RESOURCE_TYPE_GUID))
                .build()

            val assignedEmployee = AssignedEmployeeProto.newBuilder()
                .setEmployeeGuid(employeeGuid)
                .setRequirement(
                    RequirementProto.newBuilder()
                        .setGuid(requirement)
                        .build()
                )
                .setBegin(it.o.value(DutyDefinitionProtoMapping.BEGIN))
                .setEnd(it.o.value(DutyDefinitionProtoMapping.END))
                .setInfo(info)
                .setInlineEmployee(employee)
                .build()

            if (duties[parentGuid] == null) {
                Log.e(TAG, "No duties found for $parentGuid")
            }

            when (requirement) {
                // Vehicle
                Requirement.VEHICLE.value,
                Requirement.SEW.value,
                Requirement.RTW.value,
                Requirement.ITF.value,
                Requirement.HAEND.value -> {
                    duties[parentGuid] =
                        duties[parentGuid]?.toBuilder()?.addSew(assignedEmployee)?.build() ?: return@forEach
                }

                // Staff
                Requirement.EL.value,
                Requirement.RTW_RS.value,
                Requirement.HAEND_EL.value,
                Requirement.ITF_LKW.value-> {
                    duties[parentGuid] =
                        duties[parentGuid]?.toBuilder()
                            ?.addEl(assignedEmployee)
                            ?.setElSlotId(guid)
                            ?.build() ?: return@forEach
                }

                Requirement.TRAINING.value,
                Requirement.TF.value,
                Requirement.ITF_NFS.value,
                Requirement.RTW_NFS.value-> {
                    duties[parentGuid] =
                        duties[parentGuid]?.toBuilder()
                            ?.addTf(assignedEmployee)
                            ?.setTfSlotId(guid)
                            ?.build() ?: return@forEach
                }

                // Requirement.RS.value
                else -> {
                    duties[parentGuid] =
                        duties[parentGuid]?.toBuilder()
                            ?.addRs(assignedEmployee)
                            ?.setRsSlotId(guid)
                            ?.build() ?: return@forEach
                }
            }
        }

        val sortedDuties = duties.values.sortedBy { it.begin.toEpochMilli() }
        Log.d(TAG, "Parsed ${sortedDuties.size} duties")

        return sortedDuties
    }

    fun parseLoadMinimalDutyDefinitions(root: JSONObject): List<MinimalDutyDefinitionProto> {
        val data = root.value(PrepResponseMapping.DATA_AS_ARRAY)!!
        return data.map {
            val allocations = it.o.value(MinimalDutyDefinitionProtoMapping.ALLOCATION_INFO)
            val typeString = allocations?.getString(0)
            val type = when (typeString) {
                "[ SEW ]" -> DutyTypeProto.EMS
                "[ Schulung ]" -> DutyTypeProto.TRAINING
                else -> DutyTypeProto.UNKNOWN
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

            val def = MinimalDutyDefinitionProto.newBuilder()
                .setGuid(it.o.value(MinimalDutyDefinitionProtoMapping.GUID))
                .setBegin(it.o.value(MinimalDutyDefinitionProtoMapping.BEGIN))
                .setEnd(it.o.value(MinimalDutyDefinitionProtoMapping.END))
                .setType(type)
                .addAllStaff(staffList)
                .setDuration(it.o.value(MinimalDutyDefinitionProtoMapping.DURATION)!!)

            if (vehicle != null) {
                def.setVehicle(vehicle)
            }

            def.build()
        }
    }

    fun parseGetStaff(root: JSONObject): List<EmployeeProto> {
        val data = root.getJSONArray(this.DATA_ROOT_POSITION)
        val employees = mutableListOf<EmployeeProto>()

        for (i in 0 until data.length()) {
            val obj = data.getJSONObject(i)

            val staffToSkillsArray = obj.optJSONArray("staffToSkills")
            val skills = mutableSetOf<SkillProto>()
            if (staffToSkillsArray != null) {
                for (j in 0 until staffToSkillsArray.length()) {
                    val skillObject = staffToSkillsArray.getJSONObject(j)
                    val skillDataGuid = skillObject.optString("skillDataGuid")
                    skills.add(SkillProto.newBuilder().setGuid(skillDataGuid).build())
                }
            }

            val employee = EmployeeProto.newBuilder()
                .setGuid(obj.getString("dataGuid"))
                .setName(obj.getString("name"))
                .setIdentifier(obj.getString("personalnummer").trimLeadingZeros())
                .setPhone(obj.getString("telefon"))
                .setEmail(obj.getString("email"))
                .setDefaultOrg(obj.getString("externalIsRegularOrgUnit"))
                .setResourceTypeGuid(obj.getString("ressourceTypeDataGuid"))
                .addAllSkills(skills)

            val birthDate = obj.getString("birthdate")
            if (birthDate.isNotBlank()) {
                employee.setBirthdate(birthDate.toTimestamp())
            }

            employees.add(employee.build())
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

    fun parseCreateAndAllocateDuty(json: JSONObject): CreateDutyResponse? {
        val errors = json.optJSONArray("errorMessages")?.toListOfStrings().orEmpty()
        val successMsg = json.optString("successMessage", "").takeIf { it.isNotBlank() }
        val alertMsg = json.optString("alertMessage", "").takeIf { it.isNotBlank() }
        val changedId = json.optString("changedDataId", "").takeIf { it.isNotBlank() }
        val dataObj = json.optJSONObject("data")

        val duty = dataObj?.let { d ->
            val guid = d.optString("guid", "")
            val dGuid = d.optString("dataGuid", "")
            val org = d.optString("orgUnitDataGuid", "")
            val begin = d.optString("begin", "")
            val end = d.optString("end", "")

            if (guid.isBlank() || dGuid.isBlank() || org.isBlank() || begin.isNullOrBlank() || end.isNullOrBlank()) {
                null
            } else {
                CreatedDuty(
                    guid = guid,
                    dataGuid = dGuid,
                    orgUnitDataGuid = org,
                    begin = OffsetDateTime.parse(begin), // "2025-08-31T05:00:00Z"
                    end = OffsetDateTime.parse(end),
                    requirementGroupChildDataGuid = d.optString("requirementGroupChildDataGuid", "")
                        .nullIfBlank(),
                    resourceTypeDataGuid = d.optString("ressourceTypeDataGuid", "").nullIfBlank(),
                    skillDataGuid = d.optString("skillDataGuid", "").nullIfBlank(),
                    skillCharacterisationDataGuid = d.optString("skillCharacterisationDataGuid", "")
                        .nullIfBlank(),
                    shiftDataGuid = d.optString("shiftDataGuid", "").nullIfBlank(),
                    planBaseDataGuid = d.optString("planBaseDataGuid", "").nullIfBlank(),
                    planBaseEntryDataGuid = d.optString("planBaseEntryDataGuid", "").nullIfBlank(),
                    allocationDataGuid = d.optString("allocationDataGuid", "").nullIfBlank(),
                    allocationRessourceDataGuid = d.optString("allocationRessourceDataGuid", "")
                        .nullIfBlank(),
                    released = d.optInt("released", 0),
                    bookable = d.optInt("bookable", 0),
                    resourceName = d.optJSONObject("additionalInfos")
                        ?.optString("ressource_name", "").nullIfBlank()
                )
            }
        }

        val success = errors.isEmpty() && duty != null
        return CreateDutyResponse(
            success = success,
            errorMessages = errors,
            successMessage = successMsg,
            alertMessage = alertMsg,
            changedDataId = changedId,
            duty = duty
        )
    }

    // Helper Functions
    private fun JSONArray.toListOfStrings(): List<String> =
        (0 until length()).mapNotNull { idx -> optString(idx, null) }.filter { it.isNotBlank() }

    private fun String?.nullIfBlank(): String? = if (this.isNullOrBlank()) null else this
}