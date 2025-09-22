package me.emiliomini.dutyschedule.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PersonPlay: ImageVector
    get() {
        if (_PersonPlay != null) return _PersonPlay!!

        _PersonPlay = ImageVector.Builder(
            name = "Person_play",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(192f, 468f)
                lineTo(92f, 368f)
                quadToRelative(-12f, -12f, -12f, -28f)
                reflectiveQuadToRelative(12f, -28f)
                lineToRelative(100f, -100f)
                quadToRelative(12f, -12f, 28f, -12f)
                reflectiveQuadToRelative(28f, 12f)
                lineToRelative(100f, 100f)
                quadToRelative(12f, 12f, 12f, 28f)
                reflectiveQuadToRelative(-12f, 28f)
                lineTo(248f, 468f)
                quadToRelative(-12f, 12f, -28f, 12f)
                reflectiveQuadToRelative(-28f, -12f)
                moveTo(400f, 880f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(360f, 840f)
                verticalLineToRelative(-160f)
                quadToRelative(-50f, -4f, -99f, -11f)
                reflectiveQuadToRelative(-98f, -18f)
                quadToRelative(-17f, -4f, -27.5f, -19f)
                reflectiveQuadToRelative(-5.5f, -32f)
                reflectiveQuadToRelative(20.5f, -25f)
                reflectiveQuadToRelative(32.5f, -4f)
                quadToRelative(73f, 17f, 147.5f, 23f)
                reflectiveQuadToRelative(149.5f, 6f)
                reflectiveQuadToRelative(149.5f, -6f)
                reflectiveQuadTo(777f, 571f)
                quadToRelative(17f, -4f, 32.5f, 4f)
                reflectiveQuadToRelative(20.5f, 25f)
                reflectiveQuadToRelative(-5.5f, 32f)
                reflectiveQuadToRelative(-27.5f, 19f)
                quadToRelative(-49f, 11f, -98f, 18f)
                reflectiveQuadToRelative(-99f, 11f)
                verticalLineToRelative(160f)
                quadToRelative(0f, 17f, -11.5f, 28.5f)
                reflectiveQuadTo(560f, 880f)
                close()
                moveTo(220f, 384f)
                lineToRelative(44f, -44f)
                lineToRelative(-44f, -44f)
                lineToRelative(-44f, 44f)
                close()
                moveToRelative(260f, -104f)
                quadToRelative(-50f, 0f, -85f, -35f)
                reflectiveQuadToRelative(-35f, -85f)
                reflectiveQuadToRelative(35f, -85f)
                reflectiveQuadToRelative(85f, -35f)
                reflectiveQuadToRelative(85f, 35f)
                reflectiveQuadToRelative(35f, 85f)
                reflectiveQuadToRelative(-35f, 85f)
                reflectiveQuadToRelative(-85f, 35f)
                moveToRelative(0f, 280f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(400f, 480f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(480f, 400f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(560f, 480f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(480f, 560f)
                moveToRelative(0f, -360f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(520f, 160f)
                reflectiveQuadToRelative(-11.5f, -28.5f)
                reflectiveQuadTo(480f, 120f)
                reflectiveQuadToRelative(-28.5f, 11.5f)
                reflectiveQuadTo(440f, 160f)
                reflectiveQuadToRelative(11.5f, 28.5f)
                reflectiveQuadTo(480f, 200f)
                moveToRelative(191f, 260f)
                lineToRelative(-46f, -80f)
                quadToRelative(-5f, -9f, -5f, -20f)
                reflectiveQuadToRelative(5f, -20f)
                lineToRelative(46f, -80f)
                quadToRelative(5f, -10f, 14f, -15f)
                reflectiveQuadToRelative(20f, -5f)
                horizontalLineToRelative(90f)
                quadToRelative(11f, 0f, 20f, 5f)
                reflectiveQuadToRelative(14f, 15f)
                lineToRelative(46f, 80f)
                quadToRelative(5f, 9f, 5f, 20f)
                reflectiveQuadToRelative(-5f, 20f)
                lineToRelative(-46f, 80f)
                quadToRelative(-5f, 10f, -14f, 15f)
                reflectiveQuadToRelative(-20f, 5f)
                horizontalLineToRelative(-90f)
                quadToRelative(-11f, 0f, -20f, -5f)
                reflectiveQuadToRelative(-14f, -15f)
                moveToRelative(57f, -60f)
                horizontalLineToRelative(44f)
                lineToRelative(22f, -40f)
                lineToRelative(-22f, -40f)
                horizontalLineToRelative(-44f)
                lineToRelative(-22f, 40f)
                close()
                moveToRelative(22f, -40f)
            }
        }.build()

        return _PersonPlay!!
    }

private var _PersonPlay: ImageVector? = null

