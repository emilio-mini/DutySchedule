package me.emiliomini.dutyschedule.shared.ui.main.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

data class PullToRefreshOptions(
    val isRefreshing: Boolean,
    val onRefresh: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Screen(
    modifier: Modifier = Modifier,
    title: String = "",
    actions: @Composable (RowScope.() -> Unit) = {},
    bottomBar: @Composable (() -> Unit) = {},
    pullToRefresh: PullToRefreshOptions? = null,
    snackbarHost: @Composable (() -> Unit) = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title)
                },
                actions = actions
            )
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost
    ) { paddingValues ->
        if (pullToRefresh != null) {
            PullToRefreshBox(
                state = pullRefreshState,
                isRefreshing = pullToRefresh.isRefreshing,
                onRefresh = pullToRefresh.onRefresh,
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        state = pullRefreshState,
                        isRefreshing = pullToRefresh.isRefreshing
                    )
                }
            ) {
                content(paddingValues)
            }
        } else {
            content(paddingValues)
        }
    }
}
