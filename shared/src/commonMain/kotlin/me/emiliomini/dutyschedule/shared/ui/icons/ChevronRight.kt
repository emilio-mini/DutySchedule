package me.emiliomini.dutyschedule.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ChevronRight: ImageVector
    get() {
        if (_ChevronRight != null) return _ChevronRight!!

        _ChevronRight = ImageVector.Builder(
            name = "Chevron_right",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(504f, 480f)
                lineTo(348f, 324f)
                quadToRelative(-11f, -11f, -11f, -28f)
                reflectiveQuadToRelative(11f, -28f)
                reflectiveQuadToRelative(28f, -11f)
                reflectiveQuadToRelative(28f, 11f)
                lineToRelative(184f, 184f)
                quadToRelative(6f, 6f, 8.5f, 13f)
                reflectiveQuadToRelative(2.5f, 15f)
                reflectiveQuadToRelative(-2.5f, 15f)
                reflectiveQuadToRelative(-8.5f, 13f)
                lineTo(404f, 692f)
                quadToRelative(-11f, 11f, -28f, 11f)
                reflectiveQuadToRelative(-28f, -11f)
                reflectiveQuadToRelative(-11f, -28f)
                reflectiveQuadToRelative(11f, -28f)
                close()
            }
        }.build()

        return _ChevronRight!!
    }

private var _ChevronRight: ImageVector? = null

