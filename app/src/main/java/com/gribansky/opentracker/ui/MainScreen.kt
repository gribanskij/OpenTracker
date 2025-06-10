package com.gribansky.opentracker.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gribansky.opentracker.Overview
import com.gribansky.opentracker.TrackerNavHost
import com.gribansky.opentracker.navigateSingleTopTo
import com.gribansky.opentracker.trackerTabRowScreens
import com.gribansky.opentracker.ui.components.TrackerTabRow

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val startDestination =
        trackerTabRowScreens.find { it.route == currentDestination?.route } ?: Overview

    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    Scaffold(
        modifier = Modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            TrackerTabRow(
                allScreens = trackerTabRowScreens,
                onTabSelected = { newScreen ->
                    navController.navigateSingleTopTo(newScreen.route)
                },
                currentScreen = startDestination,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        TrackerNavHost(
            viewModelStoreOwner = viewModelStoreOwner,
            navController = navController,
            modifier = Modifier
                .padding(innerPadding)
                .padding(WindowInsets.safeDrawing.asPaddingValues()),
            startDestinationRoute = startDestination.route
        )
    }
}