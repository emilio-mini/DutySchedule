package me.emiliomini.dutyschedule.shared.services.scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
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

    var snackbarHostState: SnackbarHostState? = null
        private set

    fun registerSnackbar(snackbarHostState: SnackbarHostState) {
        this.snackbarHostState = snackbarHostState
    }

    fun setActionsForScreen(id: NavItemId, actions: List<Action>) {
        if (!this.actionRegistry.containsKey(id)) {
            this.actionRegistry[id] = actions
        } else {
            return
        }

        if (this.currentScreen == id) {
            this.actions = actions
        }
    }

    fun switchScreen(id: NavItemId) {
        this.currentScreen = id
        this.actions = this.actionRegistry[id] ?: emptyList()
    }

}

data class Action(
    val element: @Composable (run: () -> Unit) -> Unit,
    val callback: () -> Unit = {},
    val visible: Boolean = true
)
