package me.emiliomini.dutyschedule.shared.ui.icons

/*

                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   Copyright 2024 Google LLC

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Settings: ImageVector
    get() {
        if (_Settings != null) return _Settings!!

        _Settings = ImageVector.Builder(
            name = "settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black)
            ) {
                moveTo(370f, 880f)
                lineToRelative(-16f, -128f)
                quadToRelative(-13f, -5f, -24.5f, -12f)
                reflectiveQuadTo(307f, 724f)
                lineToRelative(-119f, 50f)
                lineTo(78f, 590f)
                lineToRelative(103f, -78f)
                quadToRelative(-1f, -7f, -1f, -13.5f)
                verticalLineToRelative(-27f)
                quadToRelative(0f, -6.5f, 1f, -13.5f)
                lineTo(78f, 370f)
                lineToRelative(110f, -184f)
                lineToRelative(119f, 50f)
                quadToRelative(11f, -8f, 23f, -15f)
                reflectiveQuadToRelative(24f, -12f)
                lineToRelative(16f, -128f)
                horizontalLineToRelative(220f)
                lineToRelative(16f, 128f)
                quadToRelative(13f, 5f, 24.5f, 12f)
                reflectiveQuadTo(653f, 236f)
                lineToRelative(119f, -50f)
                lineToRelative(110f, 184f)
                lineToRelative(-103f, 78f)
                quadToRelative(1f, 7f, 1f, 13.5f)
                verticalLineToRelative(27f)
                quadToRelative(0f, 6.5f, -2f, 13.5f)
                lineToRelative(103f, 78f)
                lineToRelative(-110f, 184f)
                lineToRelative(-118f, -50f)
                quadToRelative(-11f, 8f, -23f, 15f)
                reflectiveQuadToRelative(-24f, 12f)
                lineTo(590f, 880f)
                horizontalLineTo(370f)
                close()
                moveTo(440f, 800f)
                horizontalLineToRelative(79f)
                lineToRelative(14f, -106f)
                quadToRelative(31f, -8f, 57.5f, -23.5f)
                reflectiveQuadTo(639f, 633f)
                lineToRelative(99f, 41f)
                lineToRelative(39f, -68f)
                lineToRelative(-86f, -65f)
                quadToRelative(5f, -14f, 7f, -29.5f)
                reflectiveQuadToRelative(2f, -31.5f)
                quadToRelative(0f, -16f, -2f, -31.5f)
                reflectiveQuadTo(691f, 419f)
                lineToRelative(86f, -65f)
                lineToRelative(-39f, -68f)
                lineToRelative(-99f, 42f)
                quadToRelative(-22f, -23f, -48.5f, -38.5f)
                reflectiveQuadTo(533f, 266f)
                lineToRelative(-13f, -106f)
                horizontalLineToRelative(-79f)
                lineToRelative(-14f, 106f)
                quadToRelative(-31f, 8f, -57.5f, 23.5f)
                reflectiveQuadTo(321f, 327f)
                lineToRelative(-99f, -41f)
                lineToRelative(-39f, 68f)
                lineToRelative(86f, 65f)
                quadToRelative(-5f, 15f, -7f, 30f)
                reflectiveQuadToRelative(-2f, 31f)
                quadToRelative(0f, 15f, 2f, 30f)
                reflectiveQuadToRelative(7f, 30f)
                lineToRelative(-86f, 65f)
                lineToRelative(39f, 68f)
                lineToRelative(99f, -42f)
                quadToRelative(22f, 23f, 48.5f, 38.5f)
                reflectiveQuadTo(427f, 694f)
                lineToRelative(13f, 106f)
                close()
                moveTo(480f, 620f)
                quadToRelative(58f, 0f, 99f, -41f)
                reflectiveQuadToRelative(41f, -99f)
                quadToRelative(0f, -58f, -41f, -99f)
                reflectiveQuadToRelative(-99f, -41f)
                quadToRelative(-59f, 0f, -99.5f, 41f)
                reflectiveQuadTo(340f, 480f)
                quadToRelative(0f, 58f, 40.5f, 99f)
                reflectiveQuadToRelative(99.5f, 41f)
                close()
                moveTo(480f, 480f)
                close()
            }
        }.build()

        return _Settings!!
    }

private var _Settings: ImageVector? = null
