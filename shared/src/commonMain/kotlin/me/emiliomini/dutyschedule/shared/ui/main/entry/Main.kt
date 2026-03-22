package me.emiliomini.dutyschedule.shared.ui.main.entry

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_alarms_title
import dutyschedule.shared.generated.resources.main_archive_title
import dutyschedule.shared.generated.resources.main_dashboard_title
import dutyschedule.shared.generated.resources.main_schedule_title
import dutyschedule.shared.generated.resources.nav_alarms
import dutyschedule.shared.generated.resources.nav_archive
import dutyschedule.shared.generated.resources.nav_dashboard
import dutyschedule.shared.generated.resources.nav_schedule
import me.emiliomini.dutyschedule.shared.services.scaffold.ScaffoldService
import me.emiliomini.dutyschedule.shared.ui.icons.Alarm
import me.emiliomini.dutyschedule.shared.ui.icons.Archive
import me.emiliomini.dutyschedule.shared.ui.icons.Dashboard
import me.emiliomini.dutyschedule.shared.ui.icons.Schedule
import me.emiliomini.dutyschedule.shared.ui.main.screens.AlarmsScreen
import me.emiliomini.dutyschedule.shared.ui.main.screens.ArchiveScreen
import me.emiliomini.dutyschedule.shared.ui.main.screens.DashboardScreen
import me.emiliomini.dutyschedule.shared.ui.main.screens.ScheduleScreen
import me.emiliomini.dutyschedule.shared.ui.main.screens.SettingsScreen
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(modifier: Modifier = Modifier, onThemeModeChange: (Int) -> Unit, onDynamicColorChange: (Boolean) -> Unit, onLogout: () -> Unit, onRestart: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    ScaffoldService.registerSnackbar(snackbarHostState)
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val navItems = listOf(
        NavItem(
            id = NavItemId.DASHBOARD,
            label = stringResource(Res.string.nav_dashboard),
            title = stringResource(Res.string.main_dashboard_title),
            icon = Dashboard
        ),
        NavItem(
            id = NavItemId.SCHEDULE,
            label = stringResource(Res.string.nav_schedule),
            title = stringResource(Res.string.main_schedule_title),
            icon = Schedule
        ),
        NavItem(
            id = NavItemId.ARCHIVE,
            label = stringResource(Res.string.nav_archive),
            title = stringResource(Res.string.main_archive_title),
            icon = Archive
        ),
        NavItem(
            id = NavItemId.ALARMS,
            label = stringResource(Res.string.nav_alarms),
            title = stringResource(Res.string.main_alarms_title),
            icon = Alarm
        ),
    )

    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = {
                Text(navItems[selectedNavIndex].title)
            }, actions = {
                for (action in ScaffoldService.actions) {
                    if (action.visible) {
                        action.element(action.callback)
                    }
                }
            }, colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent)
        )
    }, bottomBar = {
        NavigationBar {
            navItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedNavIndex == index,
                    onClick = {
                        selectedNavIndex = index; ScaffoldService.switchScreen(navItems[index].id)
                    },
                    icon = {
                        Icon(
                            item.icon, contentDescription = item.label
                        )
                    },
                    label = { Text(item.label) })
            }
        }
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        when (selectedNavIndex) {
            0 -> DashboardScreen(
                paddingValues = paddingValues,
                onRestart = onRestart,
                onLogout = onLogout,
                onShowSettings = { showSettings = true }
            )

            1 -> ScheduleScreen(paddingValues = paddingValues)

            2 -> ArchiveScreen(paddingValues = paddingValues)

            3 -> AlarmsScreen(paddingValues = paddingValues)
        }
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState
        ) {
            SettingsScreen(
                onThemeModeChange = onThemeModeChange,
                onDynamicColorChange = onDynamicColorChange,
                onLogout = onLogout
            )
        }
    }
}

data class NavItem(
    val id: NavItemId, val label: String, val title: String, val icon: ImageVector
)

enum class NavItemId {
    DASHBOARD, SCHEDULE, ARCHIVE, ALARMS
}
