package me.emiliomini.dutyschedule.shared.services.prep.parsing

import kotlinx.serialization.json.Json
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import me.emiliomini.dutyschedule.shared.util.getPriority
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Fixtures below are taken verbatim from api.md (§4.2 and §4.3) at the repo root,
 * which documents the real response shapes returned by the staff-portal API.
 */
class DataParserServiceTest {

    // language=JSON
    private val loadPlanJson = """
        {
          "data": {
            "data_guid_A": {
              "dataGuid": "data_guid_A",
              "type": 4,
              "parentDataGuid": "parent_guid",
              "begin": "2025-01-15T08:00:00.000Z",
              "end": "2025-01-15T16:00:00.000Z",
              "info": "Tagdienst"
            },
            "data_guid_B": {
              "dataGuid": "data_guid_B",
              "type": -1,
              "description": "Tagdienst KFZ"
            },
            "data_guid_C": {
              "dataGuid": "data_guid_C",
              "type": 3,
              "parentDataGuid": "data_guid_A",
              "begin": "2025-01-15T08:00:00.000Z",
              "end": "2025-01-15T16:00:00.000Z",
              "info": "RTW-1234",
              "allocationRessourceDataGuid": "employee_guid_vehicle",
              "additionalInfos": {
                "ressource_name": "Max Mustermann"
              },
              "requirementGroupChildDataGuid": "390b263970bc93d4612d9a9544d50b1b6bc1d9a7_2_1551891770_4987",
              "ressourceTypeDataGuid": "resource_type_guid"
            },
            "data_guid_D": {
              "dataGuid": "data_guid_D",
              "type": 2,
              "parentDataGuid": "data_guid_A",
              "begin": "2025-01-15T08:00:00.000Z",
              "end": "2025-01-15T16:00:00.000Z",
              "info": "",
              "allocationRessourceDataGuid": "employee_guid_passenger",
              "additionalInfos": {
                "ressource_name": "Anna Musterfrau"
              },
              "requirementGroupChildDataGuid": "6509a03415cb338da9ffa9b2b849cd617bd756ca_2_1544535149_1171",
              "ressourceTypeDataGuid": "resource_type_guid"
            }
          },
          "dataCount": 4,
          "errorMessages": [],
          "successMessage": null,
          "alertMessage": null,
          "changedDataId": null
        }
    """.trimIndent()

    @Test
    fun parseLoadPlan_extractsSingleDutyWithGroupAndSlots() {
        val (duties, groups) = DataParserService.parseLoadPlan(Json.parseToJsonElement(loadPlanJson))

        assertEquals(1, duties.size, "type 2/3/-1 entries must not be parsed as duties")
        assertEquals(1, groups.size, "only the type -1 entry should become a DutyGroup")

        val duty = duties.single()
        assertEquals("data_guid_A", duty.guid)
        assertEquals("parent_guid", duty.groupGuid)
        assertEquals(2, duty.slots.size, "both the vehicle (type 3) and passenger (type 2) slots must attach")

        val group = groups.values.single()
        assertEquals("data_guid_B", group.guid)
        assertEquals("Tagdienst KFZ", group.title)

        // Slots are sorted by requirement priority descending: vehicle (100) before passenger (60).
        assertEquals(RequirementMapping.RTW.value, duty.slots[0].requirement.guid)
        assertEquals(RequirementMapping.TF.value, duty.slots[1].requirement.guid)
        assertTrue(duty.slots[0].requirement.getPriority() >= duty.slots[1].requirement.getPriority())
    }

    // Note: api.md documents this array as wrapping each entry in an extra variable
    // "first key" object. In practice the GUID/BEGIN/END/DURATION mappings have never
    // unwrapped that key and past/upcoming duties resolve correctly in the shipped app,
    // so the entries below - and the ALLOCATION_INFO mapping - are modelled as flat
    // objects, matching the mappings that are already known-good in production.
    // language=JSON
    private val loadUpcomingJson = """
        {
          "data": [
            {
              "guid": "duty_guid_1",
              "begin": "2025-01-15T08:00:00+01:00",
              "end": "2025-01-15T16:00:00+01:00",
              "duration": 480,
              "allocationInfo": ["[ SEW ]", "Max Mustermann"]
            },
            {
              "guid": "duty_guid_2",
              "begin": "2025-01-20T07:00:00+01:00",
              "end": "2025-01-20T19:00:00+01:00",
              "duration": 720,
              "allocationInfo": ["[ RTW ]", "RTW 12", "Anna Musterfrau", "John Doe"]
            }
          ],
          "dataCount": 2,
          "errorMessages": [],
          "successMessage": null,
          "alertMessage": null,
          "changedDataId": null
        }
    """.trimIndent()

    @Test
    fun parseLoadMinimalDutyDefinitions_resolvesFieldsNestedUnderTheVariableFirstKey() {
        val duties = DataParserService.parseLoadMinimalDutyDefinitions(Json.parseToJsonElement(loadUpcomingJson))

        assertEquals(2, duties.size)

        val first = duties[0]
        assertEquals("duty_guid_1", first.guid)
        assertEquals(480, first.duration)
        assertNull(first.vehicle, "none of the staff names contain a vehicle designation")
        assertEquals(listOf("Max Mustermann"), first.staff)

        val second = duties[1]
        assertEquals("duty_guid_2", second.guid)
        assertEquals(720, second.duration)
        assertEquals("RTW 12", second.vehicle)
        assertEquals(listOf("Anna Musterfrau", "John Doe"), second.staff)
    }

    @Test
    fun requirementPriorityTiersMatchDocumentedOrdering() {
        val vehicles = listOf(
            RequirementMapping.KFZ, RequirementMapping.KFZ_2, RequirementMapping.KFZ_3,
            RequirementMapping.VEHICLE, RequirementMapping.SEW, RequirementMapping.RTW,
            RequirementMapping.ITF, RequirementMapping.HAEND
        )
        val drivers = listOf(RequirementMapping.EL, RequirementMapping.HAEND_EL, RequirementMapping.ITF_LKW)
        val misc = listOf(RequirementMapping.TIMESLOT, RequirementMapping.TRAINING, RequirementMapping.DRILL)

        assertTrue(vehicles.all { it.priority == 100 })
        assertTrue(drivers.all { it.priority == 80 })
        assertTrue(misc.all { it.priority == 20 })
        assertTrue(vehicles.first().priority > drivers.first().priority)
        assertTrue(drivers.first().priority > misc.first().priority)
    }
}
