package me.emiliomini.dutyschedule.shared.ui.main.entry

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.nav_alarms
import dutyschedule.shared.generated.resources.nav_archive
import dutyschedule.shared.generated.resources.nav_dashboard
import dutyschedule.shared.generated.resources.nav_schedule
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.ui.icons.Alarm
import me.emiliomini.dutyschedule.shared.ui.icons.Archive
import me.emiliomini.dutyschedule.shared.ui.icons.Dashboard
import me.emiliomini.dutyschedule.shared.ui.icons.Schedule
import me.emiliomini.dutyschedule.shared.ui.main.screens.AlarmsScreen
import me.emiliomini.dutyschedule.shared.ui.main.screens.ArchiveScreen
import me.emiliomini.dutyschedule.shared.ui.main.screens.DashboardScreen
import me.emiliomini.dutyschedule.shared.ui.main.screens.ScheduleScreen
import me.emiliomini.dutyschedule.shared.ui.theme.DutyScheduleTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun Main(modifier: Modifier = Modifier, onLogout: () -> Unit, onRestart: () -> Unit) {
    val scope = rememberCoroutineScope()
    DutyScheduleTheme {
        var selectedItemIndex by remember { mutableIntStateOf(0) }
        val navItems = listOf(
            NavItem(
                label = stringResource(Res.string.nav_dashboard),
                icon = Dashboard
            ),
            NavItem(
                label = stringResource(Res.string.nav_schedule),
                icon = Schedule
            ),
            NavItem(
                label = stringResource(Res.string.nav_archive),
                icon = Archive
            ),
            NavItem(
                label = stringResource(Res.string.nav_alarms),
                icon = Alarm
            ),
        )

        when (selectedItemIndex) {
            0 -> DashboardScreen(
                bottomBar = {
                    NavigationBar {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedItemIndex == index,
                                onClick = { selectedItemIndex = index },
                                icon = {
                                    Icon(
                                        item.icon, contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) })
                        }
                    }
                }, onLogout = {
                    scope.launch {
                        DutyScheduleService.logout()
                        onLogout()
                    }
                }, onTriggerRestart = {
                    onRestart()
                })

            1 -> ScheduleScreen(
                bottomBar = {
                    NavigationBar {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedItemIndex == index,
                                onClick = { selectedItemIndex = index },
                                icon = {
                                    Icon(
                                        item.icon, contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) })
                        }
                    }
                })

            2 -> ArchiveScreen(
                bottomBar = {
                    NavigationBar {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedItemIndex == index,
                                onClick = { selectedItemIndex = index },
                                icon = {
                                    Icon(
                                        item.icon, contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) })
                        }
                    }
                })

            3 -> AlarmsScreen(
                bottomBar = {
                    NavigationBar {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedItemIndex == index,
                                onClick = { selectedItemIndex = index },
                                icon = {
                                    Icon(
                                        item.icon, contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) })
                        }
                    }
                })

        }
    }
}

data class NavItem(val label: String, val icon: ImageVector)
