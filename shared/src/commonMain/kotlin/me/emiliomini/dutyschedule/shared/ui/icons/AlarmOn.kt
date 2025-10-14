package me.emiliomini.dutyschedule.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * FIXME: This icon doesnt render correctly. Cause unknown. Temporarily replaced by Check until fixed.
 */
val AlarmOn: ImageVector
    get() {
        if (_AlarmOn != null) return _AlarmOn!!

        _AlarmOn = ImageVector.Builder(
            name = "Alarm_on",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveToRelative(438f, -412f)
                lineToRelative(-57f, -57f)
                quadToRelative(-12f, -12f, -28f, -12f)
                reflectiveQuadToRelative(-28f, 12f)
                reflectiveQuadToRelative(-12f, 28.5f)
                reflectiveQuadToRelative(12f, 28.5f)
                lineToRelative(85f, 86f)
                quadToRelative(12f, 12f, 28f, 12f)
                reflectiveQuadToRelative(28f, -12f)
                lineToRelative(170f, -170f)
                quadToRelative(12f, -12f, 12f, -28.5f)
                reflectiveQuadTo(636f, 407f)
                reflectiveQuadToRelative(-28.5f, -12f)
                reflectiveQuadToRelative(-28.5f, 12f)
                close()
                moveToRelative(42f, 332f)
                quadToRelative(-75f, 0f, -140.5f, -28.5f)
                reflectiveQuadToRelative(-114f, -77f)
                reflectiveQuadToRelative(-77f, -114f)
                reflectiveQuadTo(120f, 520f)
                reflectiveQuadToRelative(28.5f, -140.5f)
                reflectiveQuadToRelative(77f, -114f)
                reflectiveQuadToRelative(114f, -77f)
                reflectiveQuadTo(480f, 160f)
                reflectiveQuadToRelative(140.5f, 28.5f)
                reflectiveQuadToRelative(114f, 77f)
                reflectiveQuadToRelative(77f, 114f)
                reflectiveQuadTo(840f, 520f)
                reflectiveQuadToRelative(-28.5f, 140.5f)
                reflectiveQuadToRelative(-77f, 114f)
                reflectiveQuadToRelative(-114f, 77f)
                reflectiveQuadTo(480f, 880f)
                moveTo(82f, 292f)
                quadToRelative(-11f, -11f, -11f, -28f)
                reflectiveQuadToRelative(11f, -28f)
                lineToRelative(114f, -114f)
                quadToRelative(11f, -11f, 28f, -11f)
                reflectiveQuadToRelative(28f, 11f)
                reflectiveQuadToRelative(11f, 28f)
                reflectiveQuadToRelative(-11f, 28f)
                lineTo(138f, 292f)
                quadToRelative(-11f, 11f, -28f, 11f)
                reflectiveQuadToRelative(-28f, -11f)
                moveToRelative(796f, 0f)
                quadToRelative(-11f, 11f, -28f, 11f)
                reflectiveQuadToRelative(-28f, -11f)
                lineTo(708f, 178f)
                quadToRelative(-11f, -11f, -11f, -28f)
                reflectiveQuadToRelative(11f, -28f)
                reflectiveQuadToRelative(28f, -11f)
                reflectiveQuadToRelative(28f, 11f)
                lineToRelative(114f, 114f)
                quadToRelative(11f, 11f, 11f, 28f)
                reflectiveQuadToRelative(-11f, 28f)
                moveTo(480f, 800f)
                quadToRelative(117f, 0f, 198.5f, -81.5f)
                reflectiveQuadTo(760f, 520f)
                reflectiveQuadToRelative(-81.5f, -198.5f)
                reflectiveQuadTo(480f, 240f)
                reflectiveQuadToRelative(-198.5f, 81.5f)
                reflectiveQuadTo(200f, 520f)
                reflectiveQuadToRelative(81.5f, 198.5f)
                reflectiveQuadTo(480f, 800f)
            }
        }.build()

        return _AlarmOn!!
    }

private var _AlarmOn: ImageVector? = null


