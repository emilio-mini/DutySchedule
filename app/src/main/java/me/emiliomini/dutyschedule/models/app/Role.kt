package me.emiliomini.dutyschedule.models.app

import androidx.compose.ui.graphics.Color
import me.emiliomini.dutyschedule.util.USER_ROLES

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
            return USER_ROLES.getOrDefault(guid, NONE)
        }
    }
}