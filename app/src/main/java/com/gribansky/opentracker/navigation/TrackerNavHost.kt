package com.gribansky.opentracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gribansky.opentracker.ui.overview.OverviewScreen
import com.gribansky.opentracker.ui.history.HistoryScreen
import com.gribansky.opentracker.ui.settings.SettingsScreen


@Composable
fun TrackerNavHost(
    viewModelStoreOwner: ViewModelStoreOwner,
    navController: NavHostController,
    startDestinationRoute: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestinationRoute,
        modifier = modifier
    ) {
        composable(route = Overview.route) {
            OverviewScreen(
                viewModelStoreOwner = viewModelStoreOwner
            )
        }
        composable(route = History.route) {
            HistoryScreen(
                viewModelStoreOwner = viewModelStoreOwner,
            )
        }
        composable(route = Settings.route) {
            SettingsScreen(
                viewModelStoreOwner = it
            )
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }


