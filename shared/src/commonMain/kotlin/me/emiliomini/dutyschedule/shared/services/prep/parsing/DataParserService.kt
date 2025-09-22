@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.services.prep.parsing

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import me.emiliomini.dutyschedule.shared.api.getPlatformLogger
import me.emiliomini.dutyschedule.shared.datastores.CreateDutyResponse
import me.emiliomini.dutyschedule.shared.datastores.CreatedDuty
import me.emiliomini.dutyschedule.shared.datastores.DutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.DutyGroup
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.Message
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Org
import me.emiliomini.dutyschedule.shared.datastores.OrgItems
import me.emiliomini.dutyschedule.shared.datastores.Requirement
import me.emiliomini.dutyschedule.shared.datastores.Resource
import me.emiliomini.dutyschedule.shared.datastores.Skill
import me.emiliomini.dutyschedule.shared.datastores.Slot
import me.emiliomini.dutyschedule.shared.json.forEachElement
import me.emiliomini.dutyschedule.shared.json.mapElements
import me.emiliomini.dutyschedule.shared.json.value
import me.emiliomini.dutyschedule.shared.mappings.CreateDutyResponseMapping
import me.emiliomini.dutyschedule.shared.mappings.DutyDefinitionMapping
import me.emiliomini.dutyschedule.shared.mappings.DutyGroupMapping
import me.emiliomini.dutyschedule.shared.mappings.DutyTypeMapping
import me.emiliomini.dutyschedule.shared.mappings.EmployeeMapping
import me.emiliomini.dutyschedule.shared.mappings.MappingConstants
import me.emiliomini.dutyschedule.shared.mappings.MessageMapping
import me.emiliomini.dutyschedule.shared.mappings.MinimalDutyDefinitionMapping
import me.emiliomini.dutyschedule.shared.mappings.OrgMapping
import me.emiliomini.dutyschedule.shared.mappings.PrepResponseMapping
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import me.emiliomini.dutyschedule.shared.mappings.ResourceMapping
import me.emiliomini.dutyschedule.shared.mappings.SkillMapping
import me.emiliomini.dutyschedule.shared.mappings.Type
import me.emiliomini.dutyschedule.shared.util.getPriority
import me.emiliomini.dutyschedule.shared.util.nullIfBlank
import me.emiliomini.dutyschedule.shared.util.toEpochMilliseconds
import kotlin.time.ExperimentalTime

object DataParserService {
    private val logger = getPlatformLogger("DataParserService")

    fun parseOrgTree(root: JsonElement): OrgItems? {
        return OrgItems(
            orgs = root.mapElements({
                it.key
            }, {
                Org(
                    guid = it.key,
                    title = it.o.value(OrgMapping.TITLE) ?: Org().title,
                    abbreviation = it.o.value(OrgMapping.ABBREVIATION) ?: Org().abbreviation,
                    identifier = it.o.value(OrgMapping.IDENTIFIER) ?: Org().identifier
                )
            })
        )
    }

    fun parseLoadPlan(root: JsonElement): Pair<List<DutyDefinition>, Map<String, DutyGroup>> {
        val data = root.value(PrepResponseMapping.DATA_AS_OBJECT)
        if (data == null) {
            return Pair(emptyList(), emptyMap())
        }

        val duties = mutableMapOf<String, DutyDefinition>()
        duties.putAll(
            data.mapElements({
                it.o.value(DutyDefinitionMapping.GUID)
            }, {
                val type = it.o.value(DutyDefinitionMapping.TYPE)
                if (type != Type.TIMESLOT.value) {
                    it.skip()
                    null
                }

                DutyDefinition(
                    guid = it.o.value(DutyDefinitionMapping.GUID) ?: DutyDefinition().guid,
                    begin = it.o.value(DutyDefinitionMapping.BEGIN) ?: DutyDefinition().begin,
                    end = it.o.value(DutyDefinitionMapping.END) ?: DutyDefinition().end,
                    groupGuid = it.o.value(DutyDefinitionMapping.PARENT_GUID).nullIfBlank(),
                    info = it.o.value(DutyDefinitionMapping.INFO).nullIfBlank()
                )
            })
        )
        logger.d("Established ${duties.size} shifts")


        val groups = mutableMapOf<String, DutyGroup>()
        groups.putAll(
            data.mapElements({
                it.o.value(DutyGroupMapping.GUID)
            }, {
                val type = it.o.value(DutyDefinitionMapping.TYPE)
                if (type != Type.TIMESLOT_GROUP.value) {
                    it.skip()
                    null
                }

                DutyGroup(
                    guid = it.o.value(DutyGroupMapping.GUID) ?: DutyGroup().guid,
                    title = it.o.value(DutyGroupMapping.TITLE)
                )
            })
        )

        // Assign slots
        data.forEachElement {
            val type = it.o.value(DutyDefinitionMapping.TYPE)
            if (type == Type.TIMESLOT.value) {
                return@forEachElement
            }

            val parentGuid = it.o.value(DutyDefinitionMapping.PARENT_GUID)
            if (parentGuid == null || duties[parentGuid] == null) {
                logger.w("No duties found for $parentGuid")
                return@forEachElement
            }

            val employeeGuid = it.o.value(DutyDefinitionMapping.EMPLOYEE_GUID)
            val guid = it.o.value(DutyDefinitionMapping.GUID)
            var name = it.o.value(DutyDefinitionMapping.INLINE_NAME)
            val info = it.o.value(DutyDefinitionMapping.INFO)
            val requirement = it.o.value(DutyDefinitionMapping.REQUIREMENT)

            if (name == null || name.isBlank() || MappingConstants.EMPLOYEE_NAME_PLACEHOLDERS.contains(
                    name
                )
            ) {
                // Using INFO Tag as fallback
                name = info
            }
            val employee = Employee(
                guid = employeeGuid ?: Employee().guid,
                name = name ?: Employee().name,
                identifier = when (requirement) {
                    RequirementMapping.KFZ_2.value,
                    RequirementMapping.KFZ.value -> Employee.KFZ_NAME

                    RequirementMapping.VEHICLE.value -> Employee.VEHICLE_NAME
                    RequirementMapping.SEW.value -> Employee.SEW_NAME
                    RequirementMapping.ITF.value -> Employee.ITF_NAME
                    RequirementMapping.RTW.value -> Employee.RTW_NAME
                    RequirementMapping.HAEND.value -> Employee.HAEND_NAME
                    else -> ""
                },
                resourceTypeGuid = it.o.value(DutyDefinitionMapping.RESOURCE_TYPE_GUID)
                    ?: Employee().resourceTypeGuid
            )

            val assignedEmployee = Slot(
                guid = guid ?: Slot().guid,
                employeeGuid = employeeGuid.nullIfBlank(),
                requirement = Requirement(
                    guid = requirement ?: Requirement().guid
                ),
                begin = it.o.value(DutyDefinitionMapping.BEGIN) ?: Slot().begin,
                end = it.o.value(DutyDefinitionMapping.END) ?: Slot().end,
                info = it.o.value(DutyDefinitionMapping.INFO),
                inlineEmployee = if (employeeGuid.isNullOrBlank()) null else employee
            )

            duties[parentGuid] = duties[parentGuid]!!.copy(
                slots = listOf(
                    *duties[parentGuid]!!.slots.toTypedArray(),
                    assignedEmployee
                )
            )
        }

        var sortedDuties = duties.values.sortedBy { it.begin.toEpochMilliseconds() }
        sortedDuties = sortedDuties.map {
            val slots = it.slots.toMutableList()
            slots.sortBy { it.requirement.getPriority() * -1 }
            it.copy(slots = slots)
        }

        logger.d("Parsed ${sortedDuties.size} duties")

        return Pair(sortedDuties, groups)
    }

    fun parseLoadMinimalDutyDefinitions(root: JsonElement): List<MinimalDutyDefinition> {
        val data = root.value(PrepResponseMapping.DATA_AS_ARRAY) ?: JsonArray(emptyList())
        return data.mapElements {
            val allocations = it.o.value(MinimalDutyDefinitionMapping.ALLOCATION_INFO)
            val typeString =
                if (allocations.isNullOrEmpty()) "" else allocations[0].jsonPrimitive.contentOrNull
            val type = DutyTypeMapping.get(typeString)

            var staffList = mutableListOf<String>()
            if (allocations != null) {
                for (j in 1 until allocations.size) {
                    try {
                        staffList.add(allocations[j].jsonPrimitive.contentOrNull ?: "")
                    } catch (e: IllegalArgumentException) {
                        logger.w("Staff list contained a non-primitive value", throwable = e)
                    }
                }
            }
            staffList = staffList.filter { it.isNotBlank() }.toMutableList()
            val vehicle =
                staffList.find { staff ->
                    MappingConstants.VEHICLE_DESIGNATIONS.any {
                        staff.contains(
                            it
                        )
                    }
                }
            staffList = staffList.filter { it != vehicle }.toMutableList()

            MinimalDutyDefinition(
                guid = it.o.value(MinimalDutyDefinitionMapping.GUID)
                    ?: MinimalDutyDefinition().guid,
                begin = it.o.value(MinimalDutyDefinitionMapping.BEGIN)
                    ?: MinimalDutyDefinition().begin,
                end = it.o.value(MinimalDutyDefinitionMapping.END) ?: MinimalDutyDefinition().end,
                duration = it.o.value(MinimalDutyDefinitionMapping.DURATION)
                    ?: MinimalDutyDefinition().duration,
                type = type,
                typeString = typeString ?: "",
                vehicle = vehicle,
                staff = staffList
            )
        }
    }

    fun parseGetStaff(root: JsonElement): List<Employee> {
        val data = root.value(PrepResponseMapping.DATA_AS_ARRAY) ?: JsonArray(emptyList())
        return data.mapElements {
            val skills = mutableSetOf<String>()
            it.o.value(EmployeeMapping.STAFF_TO_SKILLS)?.forEachElement {
                val skill = it.o.value(SkillMapping.GUID)
                if (skill != null) {
                    skills.add(skill)
                }
            }

            Employee(
                guid = it.o.value(EmployeeMapping.GUID) ?: Employee().guid,
                name = it.o.value(EmployeeMapping.NAME) ?: Employee().name,
                identifier = it.o.value(EmployeeMapping.IDENTIFIER),
                phone = it.o.value(EmployeeMapping.PHONE),
                email = it.o.value(EmployeeMapping.EMAIL),
                defaultOrg = it.o.value(EmployeeMapping.DEFAULT_ORG),
                resourceTypeGuid = it.o.value(EmployeeMapping.RESOURCE_TYPE_GUID)
                    ?: Employee().resourceTypeGuid,
                birthdate = it.o.value(EmployeeMapping.BIRTHDATE),
                skills = skills.toList().map {
                    Skill(guid = it)
                }
            )
        }
    }

    fun parseGetResources(root: JsonObject): List<Resource>? {
        val data = root.value(PrepResponseMapping.DATA_AS_OBJECT)
        if (data == null) {
            return emptyList()
        }

        return data.mapElements {
            Resource(
                employeeGuid = it.o.value(ResourceMapping.EMPLOYEE_GUID) ?: "",
                messagesGuid = it.o.value(ResourceMapping.MESSAGES_GUID) ?: ""
            )
        }
    }

    fun parseGetMessages(root: JsonElement): List<Message>? {
        val data = root.value(PrepResponseMapping.DATA_AS_OBJECT)
        if (data == null) {
            return emptyList()
        }

        return data.mapElements {
            Message(
                guid = it.o.value(MessageMapping.GUID) ?: "",
                resourceGuid = it.o.value(MessageMapping.RESOURCE_GUID) ?: "",
                title = it.o.value(MessageMapping.TITLE) ?: "",
                message = it.o.value(MessageMapping.MESSAGE) ?: "",
                priority = it.o.value(MessageMapping.PRIORITY) ?: -1,
                displayFrom = it.o.value(MessageMapping.DISPLAY_FROM),
                displayTo = it.o.value(MessageMapping.DISPLAY_TO)
            )
        }
    }

    fun parseCreateAndAllocateDuty(json: JsonElement): CreateDutyResponse? {
        val errors = json.value(PrepResponseMapping.ERROR_MESSAGES)?.mapElements { it.o.jsonPrimitive.toString() }.orEmpty()
        val successMsg = json.value(PrepResponseMapping.SUCCESS_MESSAGE).nullIfBlank()
        val alertMsg = json.value(PrepResponseMapping.ALERT_MESSAGE).nullIfBlank()
        val changedId = json.value(PrepResponseMapping.CHANGED_DATA_ID).nullIfBlank()
        val dataObj = json.value(PrepResponseMapping.DATA_AS_OBJECT)

        val duty = dataObj?.let { d ->
            val guid = d.value(CreateDutyResponseMapping.GUID)
            val dGuid = d.value(CreateDutyResponseMapping.DATA_GUID)
            val org = d.value(CreateDutyResponseMapping.ORG)
            val begin = d.value(CreateDutyResponseMapping.BEGIN)
            val end = d.value(CreateDutyResponseMapping.END)

            if (guid.isNullOrBlank() || dGuid.isNullOrBlank() || org.isNullOrBlank() || begin == null || end == null) {
                null
            } else {
                CreatedDuty(
                    guid = guid,
                    dataGuid = dGuid,
                    orgUnitDataGuid = org,
                    begin = begin,
                    end = end,
                    requirementGroupChildDataGuid = d.value(CreateDutyResponseMapping.REQUIREMENT_GROUP_CHILD_DATA_GUID),
                    resourceTypeDataGuid = d.value(CreateDutyResponseMapping.RESOURCE_TYPE_DATA_GUID),
                    skillDataGuid = d.value(CreateDutyResponseMapping.SKILL_DATA_GUID),
                    skillCharacterisationDataGuid = d.value(CreateDutyResponseMapping.SKILL_CHARACTERISATION_DATA_GUID),
                    shiftDataGuid = d.value(CreateDutyResponseMapping.SHIFT_DATA_GUID),
                    planBaseDataGuid = d.value(CreateDutyResponseMapping.PLAN_BASE_DATA_GUID),
                    planBaseEntryDataGuid = d.value(CreateDutyResponseMapping.PLAN_BASE_ENTRY_DATA_GUID),
                    allocationDataGuid = d.value(CreateDutyResponseMapping.ALLOCATION_DATA_GUID),
                    allocationRessourceDataGuid = d.value(CreateDutyResponseMapping.ALLOCATION_RESOURCE_DATA_GUID),
                    released = d.value(CreateDutyResponseMapping.RELEASED) ?: CreatedDuty().released,
                    bookable = d.value(CreateDutyResponseMapping.BOOKABLE) ?: CreatedDuty().bookable,
                    resourceName = d.value(CreateDutyResponseMapping.RESOURCE_NAME)
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
}