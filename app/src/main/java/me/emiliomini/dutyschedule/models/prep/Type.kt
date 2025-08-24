package me.emiliomini.dutyschedule.models.prep

enum class Type(val value: Int) {
    WORKER(2),
    VEHICLE(3),
    TIMESLOT(4);

    companion object {
        const val POSITION = "type"
    }
}