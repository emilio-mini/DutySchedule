package me.emiliomini.dutyschedule.data.models.mapping

enum class Type(val value: Int) {
    WORKER(2),
    VEHICLE(3),
    TIMESLOT(4);

    companion object {
        val POSITION = "type";

        fun parse(value: Int): Type {
            return Type.entries.find { it.value == value } ?: TIMESLOT;
        }
    }
}