package me.emiliomini.dutyschedule.shared.mappings

import androidx.compose.ui.graphics.Color

enum class Role {
    NONE,
    DEVELOPER;

    fun colors(): List<Color> {
        return when (this) {
            DEVELOPER -> listOf(Color(0xFFFF9800), Color(0xFF2196F3), Color(0xFFFF9800))
            NONE -> emptyList()
        }
    }

    companion object {
        fun of(guid: String): Role {
            return USER_ROLES.getOrElse(guid) { NONE }
        }
    }
}

val USER_ROLES = mapOf(
    Pair("b3c6e3b21be3e9e4f0d3e8c38cc456b32c09a374_580_1656634402_5922", Role.DEVELOPER), // Miniberger Emilio
    Pair("5cb2ad972038fd6d38ac5ee583ebf05972ea8ba3_476_1693526649_893", Role.DEVELOPER), // Scheiböck Klaus
)
