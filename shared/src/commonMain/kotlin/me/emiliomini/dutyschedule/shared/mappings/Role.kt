package me.emiliomini.dutyschedule.shared.mappings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class Role {
    NONE,
    DEVELOPER;

    @Composable
    fun colors(): List<Color> {
        return if (isSystemInDarkTheme()) {
            when (this) {
                DEVELOPER -> listOf(Color(0xFFFFFF00), Color(0xFFFF9800), Color(0xFFFFFF00))
                NONE -> emptyList()
            }
        } else {
            when (this) {
                DEVELOPER -> listOf(Color(0x66000000), Color(0xFFFF9800), Color(0x66000000))
                NONE -> emptyList()
            }
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
