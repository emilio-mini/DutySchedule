package me.emiliomini.dutyschedule.shared.datastores

data class CreateDutyResponse(
    val success: Boolean = false,
    val errorMessages: List<String> = emptyList(),
    val successMessage: String? = null,
    val alertMessage: String? = null,
    val changedDataId: String? = null,
    val duty: CreatedDuty? = null
)

fun CreateDutyResponse.isDefault(): Boolean {
    return this == CreateDutyResponse()
}
