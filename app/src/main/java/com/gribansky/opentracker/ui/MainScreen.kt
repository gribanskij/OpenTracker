package com.gribansky.opentracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gribansky.opentracker.Overview
import com.gribansky.opentracker.TrackerNavHost
import com.gribansky.opentracker.navigateSingleTopTo
import com.gribansky.opentracker.trackerTabRowScreens
import com.gribansky.opentracker.ui.components.TrackerTabRow

@Composable
fun MainScreen () {

    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val startDestination =
        trackerTabRowScreens.find { it.route == currentDestination?.route } ?: Overview

    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    Scaffold(
        bottomBar = {
            TrackerTabRow(
                allScreens = trackerTabRowScreens,
                onTabSelected = { newScreen ->
                    navController.navigateSingleTopTo(newScreen.route)
                },
                currentScreen = startDestination
            )
        }
    ) { innerPadding ->
        TrackerNavHost(
            viewModelStoreOwner = viewModelStoreOwner,
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            startDestinationRoute = startDestination.route
        )
    }
}