package me.emiliomini.dutyschedule.ui.components.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SteeringWheel: ImageVector
    get() {
        if (_SteeringWheel != null) return _SteeringWheel!!

        _SteeringWheel = ImageVector.Builder(
            name = "SteeringWheel",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(480f, 880f)
                quadToRelative(-83f, 0f, -156f, -31.5f)
                reflectiveQuadTo(197f, 763f)
                reflectiveQuadToRelative(-85.5f, -127f)
                reflectiveQuadTo(80f, 480f)
                reflectiveQuadToRelative(31.5f, -156f)
                reflectiveQuadTo(197f, 197f)
                reflectiveQuadToRelative(127f, -85.5f)
                reflectiveQuadTo(480f, 80f)
                reflectiveQuadToRelative(156f, 31.5f)
                reflectiveQuadTo(763f, 197f)
                reflectiveQuadToRelative(85.5f, 127f)
                reflectiveQuadTo(880f, 480f)
                reflectiveQuadToRelative(-31.5f, 156f)
                reflectiveQuadTo(763f, 763f)
                reflectiveQuadToRelative(-127f, 85.5f)
                reflectiveQuadTo(480f, 880f)
                moveToRelative(-40f, -84f)
                verticalLineToRelative(-120f)
                quadToRelative(-60f, -12f, -102f, -54f)
                reflectiveQuadToRelative(-54f, -102f)
                horizontalLineTo(164f)
                quadToRelative(12f, 109f, 89.5f, 185f)
                reflectiveQuadTo(440f, 796f)
                moveToRelative(80f, 0f)
                quadToRelative(109f, -12f, 186.5f, -89.5f)
                reflectiveQuadTo(796f, 520f)
                horizontalLineTo(676f)
                quadToRelative(-12f, 60f, -54f, 102f)
                reflectiveQuadToRelative(-102f, 54f)
                close()
                moveTo(164f, 440f)
                horizontalLineToRelative(116f)
                lineToRelative(97f, -97f)
                quadToRelative(11f, -11f, 25.5f, -17f)
                reflectiveQuadToRelative(30.5f, -6f)
                horizontalLineToRelative(94f)
                quadToRelative(16f, 0f, 30.5f, 6f)
                reflectiveQuadToRelative(25.5f, 17f)
                lineToRelative(97f, 97f)
                horizontalLineToRelative(116f)
                quadToRelative(-15f, -121f, -105f, -200.5f)
                reflectiveQuadTo(480f, 160f)
                reflectiveQuadToRelative(-211f, 79.5f)
                reflectiveQuadTo(164f, 440f)
            }
        }.build()

        return _SteeringWheel!!
    }

private var _SteeringWheel: ImageVector? = null

