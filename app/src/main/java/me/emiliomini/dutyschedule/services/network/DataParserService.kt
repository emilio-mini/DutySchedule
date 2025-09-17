package me.emiliomini.dutyschedule.services.network

import android.util.Log
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyGroupProto
import me.emiliomini.dutyschedule.datastore.prep.duty.MinimalDutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.datastore.prep.employee.SkillProto
import me.emiliomini.dutyschedule.datastore.prep.employee.SlotProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.json.mapping.DutyDefinitionProtoMapping
import me.emiliomini.dutyschedule.json.mapping.DutyGroupProtoMapping
import me.emiliomini.dutyschedule.json.mapping.DutyTypeProtoMapping
import me.emiliomini.dutyschedule.json.mapping.EmployeeProtoMapping
import me.emiliomini.dutyschedule.json.mapping.MappingConstants
import me.emiliomini.dutyschedule.json.mapping.MessageMapping
import me.emiliomini.dutyschedule.json.mapping.MinimalDutyDefinitionProtoMapping
import me.emiliomini.dutyschedule.json.mapping.OrgProtoMapping
import me.emiliomini.dutyschedule.json.mapping.PrepResponseMapping
import me.emiliomini.dutyschedule.json.mapping.ResourceMapping
import me.emiliomini.dutyschedule.json.mapping.SkillProtoMapping
import me.emiliomini.dutyschedule.json.util.forEach
import me.emiliomini.dutyschedule.json.util.map
import me.emiliomini.dutyschedule.json.util.s
import me.emiliomini.dutyschedule.json.util.value
import me.emiliomini.dutyschedule.models.network.CreateDutyResponse
import me.emiliomini.dutyschedule.models.network.CreatedDuty
import me.emiliomini.dutyschedule.models.prep.Employee
import me.emiliomini.dutyschedule.models.prep.Message
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.models.prep.Resource
import me.emiliomini.dutyschedule.models.prep.Type
import me.emiliomini.dutyschedule.util.getPriority
import me.emiliomini.dutyschedule.util.toEpochMilli
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
                        .s(it.key) { setGuid(it) }
                        .s(it.o.value(OrgProtoMapping.TITLE)) { setTitle(it) }
                        .s(it.o.value(OrgProtoMapping.ABBREVIATION)) { setAbbreviation(it) }
                        .s(it.o.value(OrgProtoMapping.IDENTIFIER)) { setIdentifier(it) }
                        .build()
                })
            )
            .build()
    }

    fun parseLoadPlan(root: JSONObject): Pair<List<DutyDefinitionProto>, Map<String, DutyGroupProto>> {
        val data = root.value(PrepResponseMapping.DATA_AS_OBJECT)
        if (data == null) {
            return Pair(emptyList(), emptyMap())
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

                DutyDefinitionProto.newBuilder()
                    .s(it.o.value(DutyDefinitionProtoMapping.GUID)) { setGuid(it) }
                    .s(it.o.value(DutyDefinitionProtoMapping.BEGIN)) { setBegin(it) }
                    .s(it.o.value(DutyDefinitionProtoMapping.END)) { setEnd(it) }
                    .s(it.o.value(DutyDefinitionProtoMapping.PARENT_GUID), additionalCondition = { it.isNotBlank() }) { setGroupGuid(it) }
                    .s(
                        it.o.value(DutyDefinitionProtoMapping.INFO),
                        additionalCondition = { it.isNotBlank() }) { setInfo(it) }
                    .build()
            })
        )
        Log.d(TAG, "Established ${duties.size} shifts")


        val groups = mutableMapOf<String, DutyGroupProto>()
        groups.putAll(
            data.map({
                it.o.value(DutyGroupProtoMapping.GUID)
            }, {
                val type = it.o.value(DutyDefinitionProtoMapping.TYPE)
                if (type != Type.TIMESLOT_GROUP.value) {
                    it.skip()
                    null
                }

                DutyGroupProto.newBuilder()
                    .s(it.o.value(DutyGroupProtoMapping.GUID)) { setGuid(it) }
                    .s(it.o.value(DutyGroupProtoMapping.TITLE)) { setTitle(it) }
                    .build()
            })
        )

        // Assign slots
        data.forEach {
            val type = it.o.value(DutyDefinitionProtoMapping.TYPE)
            if (type == Type.TIMESLOT.value) {
                return@forEach
            }

            val parentGuid = it.o.value(DutyDefinitionProtoMapping.PARENT_GUID)
            if (parentGuid == null || duties[parentGuid] == null) {
                Log.w(TAG, "No duties found for $parentGuid")
                return@forEach
            }

            val employeeGuid = it.o.value(DutyDefinitionProtoMapping.EMPLOYEE_GUID)
            val guid = it.o.value(DutyDefinitionProtoMapping.GUID)
            var name = it.o.value(DutyDefinitionProtoMapping.INLINE_NAME)
            val info = it.o.value(DutyDefinitionProtoMapping.INFO)
            val requirement = it.o.value(DutyDefinitionProtoMapping.REQUIREMENT)

            if (name == null || name.isBlank() || MappingConstants.EMPLOYEE_NAME_PLACEHOLDERS.contains(
                    name
                )
            ) {
                // Using INFO Tag as fallback
                name = info
            }
            val employee = EmployeeProto.newBuilder()
                .s(employeeGuid) { setGuid(it) }
                .s(name) { setName(it) }
                .s(
                    when (requirement) {
                        Requirement.KFZ_2.value,
                        Requirement.KFZ.value -> Employee.Companion.KFZ_NAME
                        Requirement.VEHICLE.value -> Employee.Companion.VEHICLE_NAME
                        Requirement.SEW.value -> Employee.Companion.SEW_NAME
                        Requirement.ITF.value -> Employee.Companion.ITF_NAME
                        Requirement.RTW.value -> Employee.Companion.RTW_NAME
                        Requirement.HAEND.value -> Employee.Companion.HAEND_NAME
                        else -> ""
                    }
                ) { setIdentifier(it) }
                .s(it.o.value(DutyDefinitionProtoMapping.RESOURCE_TYPE_GUID)) {
                    setResourceTypeGuid(
                        it
                    )
                }
                .build()

            val assignedEmployee = SlotProto.newBuilder()
                .s(guid) { setGuid(it) }
                .s(employeeGuid, additionalCondition = { it.isNotBlank() }) { setEmployeeGuid(it) }
                .s(
                    RequirementProto.newBuilder()
                        .s(requirement) { setGuid(it) }
                        .build()
                ) { setRequirement(it) }
                .s(it.o.value(DutyDefinitionProtoMapping.BEGIN)) { setBegin(it) }
                .s(it.o.value(DutyDefinitionProtoMapping.END)) { setEnd(it) }
                .s(info) { setInfo(it) }
                .s(employee, additionalCondition = { employeeGuid != null && employeeGuid.isNotBlank() }) { setInlineEmployee(it) }
                .build()

            duties[parentGuid] = duties[parentGuid]!!.toBuilder().addSlots(assignedEmployee).build()
        }

        var sortedDuties = duties.values.sortedBy { it.begin.toEpochMilli() }
        sortedDuties = sortedDuties.map {
            val slots = it.slotsList.toMutableList()
            slots.sortBy { it.requirement.getPriority() * -1 }
            it.toBuilder()
                .clearSlots()
                .addAllSlots(slots)
                .build()
        }

        Log.d(TAG, "Parsed ${sortedDuties.size} duties")

        return Pair(sortedDuties, groups)
    }

    fun parseLoadMinimalDutyDefinitions(root: JSONObject): List<MinimalDutyDefinitionProto> {
        val data = root.value(PrepResponseMapping.DATA_AS_ARRAY) ?: JSONArray()
        return data.map {
            val allocations = it.o.value(MinimalDutyDefinitionProtoMapping.ALLOCATION_INFO)
            val typeString = allocations?.optString(0)
            val type = DutyTypeProtoMapping.get(typeString)

            val staffList = mutableListOf<String>()
            if (allocations != null) {
                for (j in 1 until allocations.length()) {
                    staffList.add(allocations.getString(j) ?: "")
                }
            }
            staffList.filter { it.isNotBlank() }
            val vehicle =
                staffList.find { MappingConstants.VEHICLE_DESIGNATIONS.contains(it) }
            staffList.filter { it != vehicle }

            MinimalDutyDefinitionProto.newBuilder()
                .s(it.o.value(MinimalDutyDefinitionProtoMapping.GUID)) { setGuid(it) }
                .s(it.o.value(MinimalDutyDefinitionProtoMapping.BEGIN)) { setBegin(it) }
                .s(it.o.value(MinimalDutyDefinitionProtoMapping.END)) { setEnd(it) }
                .s(it.o.value(MinimalDutyDefinitionProtoMapping.DURATION)) { setDuration(it) }
                .s(type) { setType(it) }
                .s(vehicle) { setVehicle(it) }
                .addAllStaff(staffList)
                .build()
        }
    }

    fun parseGetStaff(root: JSONObject): List<EmployeeProto> {
        val data = root.value(PrepResponseMapping.DATA_AS_ARRAY) ?: JSONArray()
        return data.map {
            val skills = mutableSetOf<String>()
            it.o.value(EmployeeProtoMapping.STAFF_TO_SKILLS)?.forEach {
                val skill = it.o.value(SkillProtoMapping.GUID)
                if (skill != null) {
                    skills.add(skill)
                }
            }

            EmployeeProto.newBuilder()
                .s(it.o.value(EmployeeProtoMapping.GUID)) { setGuid(it) }
                .s(it.o.value(EmployeeProtoMapping.NAME)) { setName(it) }
                .s(it.o.value(EmployeeProtoMapping.IDENTIFIER)) { setIdentifier(it) }
                .s(it.o.value(EmployeeProtoMapping.PHONE)) { setPhone(it) }
                .s(it.o.value(EmployeeProtoMapping.EMAIL)) { setEmail(it) }
                .s(it.o.value(EmployeeProtoMapping.DEFAULT_ORG)) { setDefaultOrg(it) }
                .s(it.o.value(EmployeeProtoMapping.RESOURCE_TYPE_GUID)) { setResourceTypeGuid(it) }
                .s(it.o.value(EmployeeProtoMapping.BIRTHDATE)) { setBirthdate(it) }
                .s(
                    skills.toList().map {
                        SkillProto.newBuilder()
                            .s(it) { setGuid(it) }
                            .build()
                    }
                ) { addAllSkills(it) }
                .build()
        }
    }

    fun parseGetResources(root: JSONObject): List<Resource>? {
        val data = root.value(PrepResponseMapping.DATA_AS_OBJECT)
        if (data == null) {
            return emptyList()
        }

        return data.map {
            Resource(
                it.o.value(ResourceMapping.EMPLOYEE_GUID) ?: "",
                it.o.value(ResourceMapping.MESSAGES_GUID) ?: ""
            )
        }
    }

    fun parseGetMessages(root: JSONObject): List<Message>? {
        val data = root.value(PrepResponseMapping.DATA_AS_OBJECT)
        if (data == null) {
            return emptyList()
        }

        return data.map {
            Message(
                it.o.value(MessageMapping.GUID) ?: "",
                it.o.value(MessageMapping.RESOURCE_GUID) ?: "",
                it.o.value(MessageMapping.TITLE) ?: "",
                it.o.value(MessageMapping.MESSAGE) ?: "",
                it.o.value(MessageMapping.PRIORITY) ?: -1,
                it.o.value(MessageMapping.DISPLAY_FROM),
                it.o.value(MessageMapping.DISPLAY_TO)
            )
        }
    }

    // TODO: Clean this function up and use mapping constants as well as utils and extensions
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