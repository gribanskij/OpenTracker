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
    override val route = "overview"
}

data object History : TrackerDestination {
    override val icon = Icons.Filled.History
    override val route = "history"
}

data object Settings : TrackerDestination {
    override val icon = Icons.Filled.Settings
    override val route = "settings"
}


// Screens to be displayed in the top RallyTabRow
val trackerTabRowScreens = listOf(Overview, History, Settings)
