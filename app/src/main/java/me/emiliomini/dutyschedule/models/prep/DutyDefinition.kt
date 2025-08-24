package me.emiliomini.dutyschedule.models.prep

import java.time.OffsetDateTime

data class DutyDefinition(
    val guid: String,
    val begin: OffsetDateTime,
    val end: OffsetDateTime,
    var sew: MutableList<AssignedEmployee> = mutableListOf(),
    var el: MutableList<AssignedEmployee> = mutableListOf(),
    var tf: MutableList<AssignedEmployee> = mutableListOf(),
    var rs: MutableList<AssignedEmployee> = mutableListOf()
) {
    companion object {
        const val BEGIN_POSITION = "begin"
        const val END_POSITION = "end"

        fun getSample(): DutyDefinition {
            val nowTime = OffsetDateTime.now()
            return DutyDefinition(
                "",
                nowTime,
                nowTime.plusHours(12),
                mutableListOf(
                    AssignedEmployee(
                        Employee("e3", "1661", Employee.SEW_NAME),
                        Requirement.SEW,
                        nowTime,
                        nowTime.plusHours(12)
                    )
                ),
                mutableListOf(
                    AssignedEmployee(
                        Employee("e1", "John Doe"),
                        Requirement.EL,
                        nowTime,
                        nowTime.plusHours(12)
                    )
                ),
                mutableListOf(
                    AssignedEmployee(
                        Employee("e0", "Your Name"),
                        Requirement.TF,
                        nowTime,
                        nowTime.plusHours(4)
                    ), AssignedEmployee(
                        Employee("e2", "Jane Doe"),
                        Requirement.TF,
                        nowTime.plusHours(4),
                        nowTime.plusHours(12)
                    )
                ),
            )
        }
    }
}
