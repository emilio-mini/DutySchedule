package me.emiliomini.dutyschedule.shared.datastores

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class CreatedDuty(
    val guid: String = "",
    val dataGuid: String = "",
    val orgUnitDataGuid: String = "",
    val begin: Instant = Instant.fromEpochSeconds(0, 0),
    val end: Instant = Instant.fromEpochSeconds(0, 0),
    val requirementGroupChildDataGuid: String? = null,
    val resourceTypeDataGuid: String? = null,
    val skillDataGuid: String? = null,
    val skillCharacterisationDataGuid: String? = null,
    val shiftDataGuid: String? = null,
    val planBaseDataGuid: String? = null,
    val planBaseEntryDataGuid: String? = null,
    val allocationDataGuid: String? = null,
    val allocationRessourceDataGuid: String? = null,
    val released: Int = 0,
    val bookable: Int = 0,
    val resourceName: String? = null
)

@OptIn(ExperimentalTime::class)
fun CreatedDuty.isDefault(): Boolean {
    return this == CreatedDuty()
}
