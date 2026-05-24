package net.harutiro.nationalweather.core.router

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.harutiro.nationalweather.R
import net.harutiro.nationalweather.core.entities.BottomNavigationItem
import net.harutiro.nationalweather.core.presenter.BottomNavigationBar
import net.harutiro.nationalweather.core.presenter.history.page.HistoryPage
import net.harutiro.nationalweather.core.presenter.map.page.MapPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Router() {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavigationItem(
            title = stringResource(id = R.string.tab_map),
            selectedIcon = Icons.Filled.Place,
            unselectedIcon = Icons.Filled.Place,
            hasNews = false,
            badgeCount = null,
            path = BottomNavigationBarRoute.MAP,
        ),
        BottomNavigationItem(
            title = stringResource(id = R.string.tab_history),
            selectedIcon = Icons.Filled.History,
            unselectedIcon = Icons.Filled.History,
            hasNews = false,
            badgeCount = null,
            path = BottomNavigationBarRoute.HISTORY,
        ),
    )

    var selectedRoute by remember { mutableStateOf(BottomNavigationBarRoute.MAP) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) })
        },
        bottomBar = {
            BottomNavigationBar(
                items = items,
                selectedItemIndex = items.indexOfFirst { it.path == selectedRoute },
            ) { index ->
                selectedRoute = items[index].path
                navController.navigate(selectedRoute.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = BottomNavigationBarRoute.MAP.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            composable(BottomNavigationBarRoute.MAP.route) { MapPage() }
            composable(BottomNavigationBarRoute.HISTORY.route) { HistoryPage() }
        }
    }
}

enum class BottomNavigationBarRoute(val route: String) {
    MAP("map"),
    HISTORY("history"),
}
