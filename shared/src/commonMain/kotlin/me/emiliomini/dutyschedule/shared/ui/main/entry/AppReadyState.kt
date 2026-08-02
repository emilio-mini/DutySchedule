package me.emiliomini.dutyschedule.shared.ui.main.entry

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Flips once the initial load has finished. Platform entry points hold their splash screen until
 * then, so the splash animation runs for exactly as long as the startup work actually takes
 */
object AppReadyState {
    private val readyState = MutableStateFlow(false)

    val isReady = readyState.asStateFlow()

    fun markReady() {
        readyState.value = true
    }
}
