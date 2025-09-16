package me.emiliomini.dutyschedule.services.prep.demo

import me.emiliomini.dutyschedule.datastore.prep.StatisticsProto
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyTypeProto
import me.emiliomini.dutyschedule.datastore.prep.duty.MinimalDutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.duty.UpcomingDutyItemsProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeItemsProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.datastore.prep.employee.SlotProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgDayProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.util.toTimestamp

/*
    Disclaimer

    All organization names, personal information, titles, abbreviations, identifiers, GUIDs,
    timestamps, phone numbers, email addresses, and any other data presented in the examples
    below are entirely fictional and were generated solely for illustrative purposes. None of
    the entities, individuals, or contact details correspond to real persons, companies, or
    services. Any resemblance to actual organizations or individuals is purely coincidental.
    This content is used only as a sample or template to demonstrate the functionality and
    appearance of this application without the need to provide actual credentials or connect
    to a server.
 */
object DemoData {
    val statistics: StatisticsProto = StatisticsProto.newBuilder()
        .setMinutesServed(4000)
        .build()

    val self: EmployeeProto = EmployeeProto.newBuilder()
        .setGuid("d4b7c9e2-3f6a-41b8-9e57-2a6f8c1d5b9e")
        .setName("Aisha Patel")
        .setIdentifier("73921")
        .setPhone("+1 415 867 5309")
        .setEmail("aisha.patel@example.org")
        .setDefaultOrg("5124")
        .setBirthdate("1988-03-22T00:00:00Z".toTimestamp())
        .build()

    val employees: List<EmployeeProto> = listOf(
        self,
        EmployeeProto.newBuilder()
            .setGuid("a1f2c9e4-6b3d-48e1-9a2f-5d7c9b1e8f33")
            .setName("Maya Alvarez")
            .setIdentifier("48327")
            .setPhone("+1 202 555 0143")
            .setEmail("maya.alvarez@example.com")
            .setDefaultOrg("1023")
            .setBirthdate("1992-07-15T00:00:00Z".toTimestamp())
            .build(),

        EmployeeProto.newBuilder()
            .setGuid("d4e5b6a7-9c01-4f2b-8d3e-2a6f9c0b7e55")
            .setName("Liam O'Connor")
            .setIdentifier("75019")
            .setPhone("+44 7700 900 123")
            .setEmail("liam.oconnor@example.co.uk")
            .setDefaultOrg("1023")
            .setBirthdate("1987-03-22T00:00:00Z".toTimestamp())
            .build(),

        EmployeeProto.newBuilder()
            .setGuid("f9c2d3e1-5b6a-4c7d-9e2f-3a1b8c0d4e66")
            .setName("Sofia Rossi")
            .setIdentifier("16284")
            .setPhone("+39 327 555 0198")
            .setEmail("sofia.rossi@example.it")
            .setDefaultOrg("1023")
            .setBirthdate("1995-11-08T00:00:00Z".toTimestamp())
            .build(),

        EmployeeProto.newBuilder()
            .setGuid("b3e7c1d2-8f4a-4b9c-9d5e-6a2f1c3b7e88")
            .setName("Jae‑Hyun Kim")
            .setIdentifier("90457")
            .setPhone("+82 10 5555 6789")
            .setEmail("jaehyun.kim@example.kr")
            .setDefaultOrg("2047")
            .setBirthdate("1990-05-30T00:00:00Z".toTimestamp())
            .build(),

        EmployeeProto.newBuilder()
            .setGuid("c5d8e9f0-1a2b-4c3d-9e5f-7b8c9d0e1f22")
            .setName("Aisha Patel")
            .setIdentifier("31792")
            .setPhone("+91 98765 43210")
            .setEmail("aisha.patel@example.in")
            .setDefaultOrg("2047")
            .setBirthdate("1998-02-14T00:00:00Z".toTimestamp())
            .build(),

        EmployeeProto.newBuilder()
            .setGuid("e2f3a4b5-6c7d-48e9-9a0b-1c2d3e4f5a66")
            .setName("Ethan Miller")
            .setIdentifier("62841")
            .setPhone("+1 415 555 0127")
            .setEmail("ethan.miller@example.com")
            .setDefaultOrg("3071")
            .setBirthdate("1985-09-03T00:00:00Z".toTimestamp())
            .build(),

        EmployeeProto.newBuilder()
            .setGuid("f1a2b3c4-5d6e-47f8-9a0b-2c3d4e5f6a77")
            .setName("Nina Johansson")
            .setIdentifier("54028")
            .setPhone("+46 70 123 45 67")
            .setEmail("nina.johansson@example.se")
            .setDefaultOrg("4099")
            .setBirthdate("1993-12-19T00:00:00Z".toTimestamp())
            .build(),

        EmployeeProto.newBuilder()
            .setGuid("a9b8c7d6-5e4f-43a2-9b1c-8d7e6f5a4b33")
            .setName("Carlos Mendes")
            .setIdentifier("83904")
            .setPhone("+351 912 345 678")
            .setEmail("carlos.mendes@example.pt")
            .setDefaultOrg("4099")
            .setBirthdate("1991-06-25T00:00:00Z".toTimestamp())
            .build(),

        EmployeeProto.newBuilder()
            .setGuid("d3e4f5a6-7b8c-49d0-9e1f-2a3b4c5d6e88")
            .setName("Yara Haddad")
            .setIdentifier("27419")
            .setPhone("+966 5 555 5555")
            .setEmail("yara.haddad@example.sa")
            .setDefaultOrg("5124")
            .setBirthdate("1997-08-11T00:00:00Z".toTimestamp())
            .build(),

        EmployeeProto.newBuilder()
            .setGuid("b2c3d4e5-6f7a-48b9-9c0d-3e4f5a6b7c99")
            .setName("Olivier Dupont")
            .setIdentifier("59173")
            .setPhone("+33 6 12 34 56 78")
            .setEmail("olivier.dupont@example.fr")
            .setDefaultOrg("5124")
            .setBirthdate("1989-04-02T00:00:00Z".toTimestamp())
            .build()
    )
    val employeeItems: EmployeeItemsProto = EmployeeItemsProto.newBuilder()
        .putAllEmployees(employees.associateBy { it.guid })
        .build()

    val orgs: List<OrgProto> = listOf(
        OrgProto.newBuilder()
            .setGuid("a1f2c9e4-6b3d-48e1-9a2f-5d7c9b1e8f33")
            .setTitle("Fire Rescue Station North")
            .setAbbreviation("FRS-N")
            .setIdentifier("1023")
            .build(),

        OrgProto.newBuilder()
            .setGuid("d4e5b6a7-9c01-4f2b-8d3e-2a6f9c0b7e55")
            .setTitle("Medical Aid Station West")
            .setAbbreviation("MAS-W")
            .setIdentifier("2047")
            .build(),

        OrgProto.newBuilder()
            .setGuid("f9c2d3e1-5b6a-4c7d-9e2f-3a1b8c0d4e66")
            .setTitle("Emergency Ops Station East")
            .setAbbreviation("EOS-E")
            .setIdentifier("3071")
            .build(),

        OrgProto.newBuilder()
            .setGuid("b3e7c1d2-8f4a-4b9c-9d5e-6a2f1c3b7e88")
            .setTitle("Hazardous Materials Station Central")
            .setAbbreviation("HMS-C")
            .setIdentifier("4099")
            .build(),

        OrgProto.newBuilder()
            .setGuid("c5d8e9f0-1a2b-4c3d-9e5f-7b8c9d0e1f22")
            .setTitle("Search & Rescue Station South")
            .setAbbreviation("SRS-S")
            .setIdentifier("5124")
            .build()
    )
    val orgItems: OrgItemsProto = OrgItemsProto.newBuilder()
        .putAllOrgs(orgs.associateBy { it.guid })
        .build()

    val allowedOrgs: List<String> = listOf("c5d8e9f0-1a2b-4c3d-9e5f-7b8c9d0e1f22")

    val upcoming: List<MinimalDutyDefinitionProto> = listOf(
        MinimalDutyDefinitionProto.newBuilder()
            .setGuid("a1f2c9e4-6b3d-48e1-9a2f-5d7c9b1e8f33")
            .setBegin("2025-09-01T05:00:00Z".toTimestamp())
            .setEnd("2025-09-01T17:00:00Z".toTimestamp())
            .setType(DutyTypeProto.EMS)
            .setVehicle("EVA 1538")
            .addAllStaff(listOf("Maya Alvarez"))
            .setDuration(720)
            .build(),

        MinimalDutyDefinitionProto.newBuilder()
            .setGuid("d4e5b6a7-9c01-4f2b-8d3e-2a6f9c0b7e55")
            .setBegin("2025-09-02T06:00:00Z".toTimestamp())
            .setEnd("2025-09-02T18:00:00Z".toTimestamp())
            .setType(DutyTypeProto.EMS)
            .setVehicle("EVA 2741")
            .addAllStaff(listOf("Liam O'Connor", "Sofia Rossi"))
            .setDuration(720)
            .build(),

        MinimalDutyDefinitionProto.newBuilder()
            .setGuid("f9c2d3e1-5b6a-4c7d-9e2f-3a1b8c0d4e66")
            .setBegin("2025-09-03T09:00:00Z".toTimestamp())
            .setEnd("2025-09-03T15:00:00Z".toTimestamp())
            .setType(DutyTypeProto.TRAINING)
            .setVehicle("Trainer Van 402")
            .addAllStaff(listOf("Jae‑Hyun Kim"))
            .setDuration(360)
            .build(),

        MinimalDutyDefinitionProto.newBuilder()
            .setGuid("b3e7c1d2-8f4a-4b9c-9d5e-6a2f1c3b7e88")
            .setBegin("2025-09-04T07:00:00Z".toTimestamp())
            .setEnd("2025-09-04T15:00:00Z".toTimestamp())
            .setType(DutyTypeProto.TRAINING)
            .setVehicle("Trainer Bus 108")
            .addAllStaff(listOf("Aisha Patel", "Ethan Miller"))
            .setDuration(480)
            .build(),
    )
    val upcomingItems: UpcomingDutyItemsProto = UpcomingDutyItemsProto.newBuilder()
        .addAllMinimalDutyDefinitions(upcoming)
        .build()

    val orgDay: OrgDayProto = OrgDayProto.newBuilder()
        .setOrgGuid("c5d8e9f0-1a2b-4c3d-9e5f-7b8c9d0e1f22")
        .setDate("2025-09-01T00:00:00Z".toTimestamp())
        .addDayShift(
            DutyDefinitionProto.newBuilder()
                .setGuid("a1f2c9e4-6b3d-48e1-9a2f-5d7c9b1e8f33")
                .setBegin("2025-09-01T05:00:00Z".toTimestamp())
                .setEnd("2025-09-01T17:00:00Z".toTimestamp())
                .addSlots(
                    SlotProto.newBuilder()
                        .setBegin("2025-09-01T05:00:00Z".toTimestamp())
                        .setEnd("2025-09-01T17:00:00Z".toTimestamp())
                        .setRequirement(
                            RequirementProto.newBuilder().setGuid(Requirement.SEW.value).build()
                        )
                        .setInlineEmployee(
                            EmployeeProto.newBuilder()
                                .setIdentifier("Amb 403")
                                .setName("Ambulance 403")
                                .build()
                        )
                        .build()
                )
                .addSlots(
                    SlotProto.newBuilder()
                        .setBegin("2025-09-01T05:00:00Z".toTimestamp())
                        .setEnd("2025-09-01T17:00:00Z".toTimestamp())
                        .setEmployeeGuid("a9b8c7d6-5e4f-43a2-9b1c-8d7e6f5a4b33")
                        .setRequirement(
                            RequirementProto.newBuilder().setGuid(Requirement.EL.value).build()
                        )
                        .build()
                )
                .addSlots(
                    SlotProto.newBuilder()
                        .setBegin("2025-09-01T05:00:00Z".toTimestamp())
                        .setEnd("2025-09-01T17:00:00Z".toTimestamp())
                        .setEmployeeGuid("d4b7c9e2-3f6a-41b8-9e57-2a6f8c1d5b9e")
                        .setRequirement(
                            RequirementProto.newBuilder().setGuid(Requirement.RTW_NFS.value).build()
                        )
                        .build()
                )
                .build()
        )
        .addDayShift(
            DutyDefinitionProto.newBuilder()
                .setGuid("a2f2c9e4-6b3d-45e1-9a2f-5d7c9b1e8f33")
                .setBegin("2025-09-01T05:00:00Z".toTimestamp())
                .setEnd("2025-09-01T17:00:00Z".toTimestamp())
                .addSlots(
                    SlotProto.newBuilder()
                        .setBegin("2025-09-01T05:00:00Z".toTimestamp())
                        .setEnd("2025-09-01T17:00:00Z".toTimestamp())
                        .setRequirement(
                            RequirementProto.newBuilder().setGuid(Requirement.SEW.value).build()
                        )
                        .setInlineEmployee(
                            EmployeeProto.newBuilder()
                                .setIdentifier("Amb 1238")
                                .setName("Ambulance 1238")
                                .build()
                        )
                        .build()
                )
                .addSlots(
                    SlotProto.newBuilder()
                        .setBegin("2025-09-01T05:00:00Z".toTimestamp())
                        .setEnd("2025-09-01T12:00:00Z".toTimestamp())
                        .setEmployeeGuid("d3e4f5a6-7b8c-49d0-9e1f-2a3b4c5d6e88")
                        .setRequirement(
                            RequirementProto.newBuilder().setGuid(Requirement.EL.value).build()
                        )
                        .build()
                )
                .addSlots(
                    SlotProto.newBuilder()
                        .setBegin("2025-09-01T12:00:00Z".toTimestamp())
                        .setEnd("2025-09-01T17:00:00Z".toTimestamp())
                        .setEmployeeGuid("b3e7c1d2-8f4a-4b9c-9d5e-6a2f1c3b7e88")
                        .setRequirement(
                            RequirementProto.newBuilder().setGuid(Requirement.EL.value).build()
                        )
                        .build()
                )
                .addSlots(
                    SlotProto.newBuilder()
                        .setBegin("2025-09-01T05:00:00Z".toTimestamp())
                        .setEnd("2025-09-01T17:00:00Z".toTimestamp())
                        .setEmployeeGuid("b2c3d4e5-6f7a-48b9-9c0d-3e4f5a6b7c99")
                        .setRequirement(
                            RequirementProto.newBuilder().setGuid(Requirement.TF.value).build()
                        )
                        .build()
                )
                .build()
        )
        .build()
}
