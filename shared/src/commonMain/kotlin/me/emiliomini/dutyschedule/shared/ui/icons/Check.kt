package me.emiliomini.dutyschedule.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Check: ImageVector
    get() {
        if (_Check != null) return _Check!!

        _Check = ImageVector.Builder(
            name = "Check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveToRelative(382f, -354f)
                lineToRelative(339f, -339f)
                quadToRelative(12f, -12f, 28f, -12f)
                reflectiveQuadToRelative(28f, 12f)
                reflectiveQuadToRelative(12f, 28.5f)
                reflectiveQuadToRelative(-12f, 28.5f)
                lineTo(410f, 692f)
                quadToRelative(-12f, 12f, -28f, 12f)
                reflectiveQuadToRelative(-28f, -12f)
                lineTo(182f, 520f)
                quadToRelative(-12f, -12f, -11.5f, -28.5f)
                reflectiveQuadTo(183f, 463f)
                reflectiveQuadToRelative(28.5f, -12f)
                reflectiveQuadToRelative(28.5f, 12f)
                close()
            }
        }.build()

        return _Check!!
    }

private var _Check: ImageVector? = null

