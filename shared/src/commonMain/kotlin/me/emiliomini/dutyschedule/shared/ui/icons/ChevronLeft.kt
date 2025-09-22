package me.emiliomini.dutyschedule.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ChevronLeft: ImageVector
    get() {
        if (_ChevronLeft != null) return _ChevronLeft!!

        _ChevronLeft = ImageVector.Builder(
            name = "Chevron_left",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveToRelative(432f, -480f)
                lineToRelative(156f, 156f)
                quadToRelative(11f, 11f, 11f, 28f)
                reflectiveQuadToRelative(-11f, 28f)
                reflectiveQuadToRelative(-28f, 11f)
                reflectiveQuadToRelative(-28f, -11f)
                lineTo(348f, 508f)
                quadToRelative(-6f, -6f, -8.5f, -13f)
                reflectiveQuadToRelative(-2.5f, -15f)
                reflectiveQuadToRelative(2.5f, -15f)
                reflectiveQuadToRelative(8.5f, -13f)
                lineToRelative(184f, -184f)
                quadToRelative(11f, -11f, 28f, -11f)
                reflectiveQuadToRelative(28f, 11f)
                reflectiveQuadToRelative(11f, 28f)
                reflectiveQuadToRelative(-11f, 28f)
                close()
            }
        }.build()

        return _ChevronLeft!!
    }

private var _ChevronLeft: ImageVector? = null

