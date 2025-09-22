package me.emiliomini.dutyschedule.shared.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val DeleteSweep: ImageVector
    get() {
        if (_DeleteSweep != null) return _DeleteSweep!!

        _DeleteSweep = ImageVector.Builder(
            name = "Delete_sweep",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(200f, 760f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(120f, 680f)
                verticalLineToRelative(-360f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(80f, 280f)
                reflectiveQuadToRelative(11.5f, -28.5f)
                reflectiveQuadTo(120f, 240f)
                horizontalLineToRelative(120f)
                verticalLineToRelative(-20f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(280f, 180f)
                horizontalLineToRelative(80f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(400f, 220f)
                verticalLineToRelative(20f)
                horizontalLineToRelative(120f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(560f, 280f)
                reflectiveQuadToRelative(-11.5f, 28.5f)
                reflectiveQuadTo(520f, 320f)
                verticalLineToRelative(360f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(440f, 760f)
                close()
                moveToRelative(440f, -40f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(600f, 680f)
                reflectiveQuadToRelative(11.5f, -28.5f)
                reflectiveQuadTo(640f, 640f)
                horizontalLineToRelative(80f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(760f, 680f)
                reflectiveQuadToRelative(-11.5f, 28.5f)
                reflectiveQuadTo(720f, 720f)
                close()
                moveToRelative(0f, -160f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(600f, 520f)
                reflectiveQuadToRelative(11.5f, -28.5f)
                reflectiveQuadTo(640f, 480f)
                horizontalLineToRelative(160f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(840f, 520f)
                reflectiveQuadToRelative(-11.5f, 28.5f)
                reflectiveQuadTo(800f, 560f)
                close()
                moveToRelative(0f, -160f)
                quadToRelative(-17f, 0f, -28.5f, -11.5f)
                reflectiveQuadTo(600f, 360f)
                reflectiveQuadToRelative(11.5f, -28.5f)
                reflectiveQuadTo(640f, 320f)
                horizontalLineToRelative(200f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(880f, 360f)
                reflectiveQuadToRelative(-11.5f, 28.5f)
                reflectiveQuadTo(840f, 400f)
                close()
                moveToRelative(-440f, -80f)
                verticalLineToRelative(360f)
                horizontalLineToRelative(240f)
                verticalLineToRelative(-360f)
                close()
            }
        }.build()

        return _DeleteSweep!!
    }

private var _DeleteSweep: ImageVector? = null

