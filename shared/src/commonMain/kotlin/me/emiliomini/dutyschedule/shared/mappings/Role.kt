package me.emiliomini.dutyschedule.shared.mappings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class Role {
    NONE,
    FIRST_USER,
    DEVELOPER;

    @Composable
    fun colors(): List<Color> {
        return if (isSystemInDarkTheme()) {
            when (this) {
                DEVELOPER -> listOf(Color(0xFFFFFF00), Color(0xFFFF9800), Color(0xFFFFFF00))
                FIRST_USER -> listOf(Color(0xFFB62ECE), Color(0xFFCE93D8), Color(0xFFB62ECE))
                NONE -> emptyList()
            }
        } else {
            when (this) {
                DEVELOPER -> listOf(Color(0x66000000), Color(0xFFFF9800), Color(0x66000000))
                FIRST_USER -> listOf(Color(0x66000000), Color(0xFF673AB7), Color(0x66000000))
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
    Pair("5ba809a5b0e0e4cc4406a054838b9e06011b4a29_82565_1574428744_0754", Role.FIRST_USER), // Plachy Tamara
)
