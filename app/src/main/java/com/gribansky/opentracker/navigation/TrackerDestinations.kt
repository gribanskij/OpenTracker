package com.gribansky.opentracker.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.gribansky.opentracker.R


sealed interface TrackerDestination {
    val icon: ImageVector
    @get:StringRes
    val labelResId: Int
    val label: String @Composable get() = stringResource(labelResId)
    val route: String
}

data object Overview : TrackerDestination {
    override val icon = Icons.Filled.Dashboard
    override val labelResId: Int
        get() = R.string.overview
    override val route = "overview"
}

data object History : TrackerDestination {
    override val icon = Icons.Filled.History
    override val labelResId: Int
        get() = R.string.history
    override val route = "log"
}

data object Settings : TrackerDestination {
    override val icon = Icons.Filled.Settings
    override val labelResId: Int
        get() = R.string.settings
    override val route = "settings"
}


val trackerTabRowScreens = listOf(Overview, History, Settings)
