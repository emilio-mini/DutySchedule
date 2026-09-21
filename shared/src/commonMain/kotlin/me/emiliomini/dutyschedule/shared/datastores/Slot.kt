package me.emiliomini.dutyschedule.shared.datastores

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Slot(
    @ProtoNumber(1)
    val guid: String = "",
    @ProtoNumber(2)
    val employeeGuid: String? = null,
    @ProtoNumber(3)
    val requirement: Requirement = Requirement(),
    @ProtoNumber(4)
    val begin: Timestamp = Timestamp(),
    @ProtoNumber(5)
    val end: Timestamp = Timestamp(),
    @ProtoNumber(6)
    val info: String? = null,
    @ProtoNumber(7)
    /**
     * Null on a slot nobody is on, which is what the parser produces for one. The default has to be
     * null rather than an empty employee: protobuf cannot write an explicit null over a non-null
     * default, so a real null would fail to encode and the whole record would go unsaved
     */
    val inlineEmployee: Employee? = null
) : MultiplatformDataModel

fun Slot.isDefault(): Boolean {
    return this == Slot()
}
