package com.gribansky.opentracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector


sealed interface TrackerDestination {
    val icon: ImageVector
    val route: String
}

data object Overview : TrackerDestination {
    override val icon = Icons.Filled.Dashboard
    override val route = "обзор"
}

data object History : TrackerDestination {
    override val icon = Icons.Filled.History
    override val route = "журнал"
}

data object Settings : TrackerDestination {
    override val icon = Icons.Filled.Settings
    override val route = "настройки"
}


val trackerTabRowScreens = listOf(Overview, History, Settings)
