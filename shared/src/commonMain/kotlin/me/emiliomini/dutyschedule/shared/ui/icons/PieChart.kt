package me.emiliomini.dutyschedule.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PieChartIcon: ImageVector
    get() {
        if (_PieChart != null) return _PieChart!!

        _PieChart = ImageVector.Builder(
            name = "Pie_chart",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(520f, 440f)
                horizontalLineToRelative(278f)
                quadToRelative(-15f, -110f, -91.5f, -186.5f)
                reflectiveQuadTo(520f, 162f)
                close()
                moveToRelative(-80f, 358f)
                verticalLineToRelative(-636f)
                quadToRelative(-121f, 15f, -200.5f, 105.5f)
                reflectiveQuadTo(160f, 480f)
                reflectiveQuadToRelative(79.5f, 212.5f)
                reflectiveQuadTo(440f, 798f)
                moveToRelative(80f, 0f)
                quadToRelative(110f, -14f, 187f, -91f)
                reflectiveQuadToRelative(91f, -187f)
                horizontalLineTo(520f)
                close()
                moveToRelative(-40f, 82f)
                quadToRelative(-83f, 0f, -156f, -31.5f)
                reflectiveQuadTo(197f, 763f)
                reflectiveQuadToRelative(-85.5f, -127f)
                reflectiveQuadTo(80f, 480f)
                reflectiveQuadToRelative(31.5f, -156f)
                reflectiveQuadTo(197f, 197f)
                reflectiveQuadToRelative(127f, -85.5f)
                reflectiveQuadTo(480f, 80f)
                reflectiveQuadToRelative(155.5f, 31.5f)
                reflectiveQuadToRelative(127f, 86f)
                reflectiveQuadToRelative(86f, 127f)
                reflectiveQuadTo(880f, 480f)
                quadToRelative(0f, 82f, -31.5f, 155f)
                reflectiveQuadTo(763f, 762.5f)
                reflectiveQuadToRelative(-127f, 86f)
                reflectiveQuadTo(480f, 880f)
            }
        }.build()

        return _PieChart!!
    }

private var _PieChart: ImageVector? = null

