
package com.gribansky.opentracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gribansky.opentracker.ui.dashboard.OverviewScreen
import com.gribansky.opentracker.ui.history.HistoryScreen
import com.gribansky.opentracker.ui.settings.SettingsScreen


@Composable
fun TrackerNavHost(
    viewModelStoreOwner: ViewModelStoreOwner,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Overview.route,
        modifier = modifier
    ) {
        composable(route = Overview.route) {
            OverviewScreen(
                viewModelStoreOwner = viewModelStoreOwner,
                onClickSeeAllAccounts = {
                    //navController.navigateSingleTopTo(Accounts.route)
                },
                onClickSeeAllBills = {
                    //navController.navigateSingleTopTo(Bills.route)
                },
                onAccountClick = { accountType ->
                    //navController.navigateToSingleAccount(accountType)
                }
            )
        }
        composable(route = History.route) {
            HistoryScreen(
                viewModelStoreOwner = viewModelStoreOwner,
                onAccountClick = { accountType ->
                    //navController.navigateToSingleAccount(accountType)
                }
            )
        }
        composable(route = Settings.route) {
            SettingsScreen()
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


