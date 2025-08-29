package me.emiliomini.dutyschedule.models.network

import java.time.OffsetDateTime

data class CreateDutyResponse(
        val success: Boolean,
        val errorMessages: List<String>,
        val successMessage: String?,
        val alertMessage: String?,
        val changedDataId: String?,
        val duty: CreatedDuty?
)

data class CreatedDuty(
        val guid: String,
        val dataGuid: String,
        val orgUnitDataGuid: String,
        val begin: OffsetDateTime,
        val end: OffsetDateTime,
        val requirementGroupChildDataGuid: String?,
        val resourceTypeDataGuid: String?,
        val skillDataGuid: String?,
        val skillCharacterisationDataGuid: String?,
        val shiftDataGuid: String?,
        val planBaseDataGuid: String?,
        val planBaseEntryDataGuid: String?,
        val allocationDataGuid: String?,
        val allocationRessourceDataGuid: String?,
        val released: Int,
        val bookable: Int,
        val resourceName: String?
)