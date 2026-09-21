package me.emiliomini.dutyschedule.shared.services.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.emiliomini.dutyschedule.shared.ui.main.entry.NavItemId

object ScaffoldService {
    var currentScreen by mutableStateOf(NavItemId.DASHBOARD)
        private set
    var actions by mutableStateOf<List<Action>>(emptyList())
        private set

    private var actionRegistry: MutableMap<NavItemId, List<Action>> = mutableMapOf()
    private var pendingScheduleFocus: ScheduleFocus? = null

    var snackbarHostState: SnackbarHostState? = null
        private set

    fun registerSnackbar(snackbarHostState: SnackbarHostState) {
        this.snackbarHostState = snackbarHostState
    }

    internal fun setActionsForScreen(id: NavItemId, actions: List<Action>) {
        this.actionRegistry[id] = actions

        if (this.currentScreen == id) {
            this.actions = actions
        }
    }

    internal fun clearActionsForScreen(id: NavItemId) {
        this.actionRegistry.remove(id)

        if (this.currentScreen == id) {
            this.actions = emptyList()
        }
    }

    fun switchScreen(id: NavItemId) {
        this.currentScreen = id
        this.actions = this.actionRegistry[id] ?: emptyList()
    }

    /**
     * Sends the user to the schedule pointed at a particular duty. The schedule is rebuilt from
     * scratch on every tab switch, so the request is left here for it to pick up as it composes
     * rather than pushed into it
     */
    fun focusSchedule(focus: ScheduleFocus) {
        this.pendingScheduleFocus = focus
        switchScreen(NavItemId.SCHEDULE)
    }

    /** Takes the pending request, if any, and clears it so re-entering the tab starts clean */
    fun consumeScheduleFocus(): ScheduleFocus? {
        val focus = this.pendingScheduleFocus
        this.pendingScheduleFocus = null
        return focus
    }
}

/** Which org and which duty the schedule should open on; the week is derived from [dutyBegin] */
data class ScheduleFocus(
    val orgGuid: String,
    val dutyBegin: Long,
    val planDutyGuid: String
)

/**
 * Publishes a screen's top bar actions while that screen is composed. Switching tabs disposes the
 * screen, so the actions have to be registered again on re-entry to stay bound to live state
 */
@Composable
fun ScreenActions(id: NavItemId, actions: () -> List<Action>) {
    DisposableEffect(id) {
        ScaffoldService.setActionsForScreen(id, actions())
        onDispose {
            ScaffoldService.clearActionsForScreen(id)
        }
    }
}

data class Action(
    val element: @Composable (run: () -> Unit) -> Unit,
    val callback: () -> Unit = {},
    val visible: Boolean = true
)
