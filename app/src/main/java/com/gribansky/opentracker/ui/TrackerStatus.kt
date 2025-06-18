package com.gribansky.opentracker.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.gribansky.opentracker.R

enum class TrackerStatus(
    @StringRes val labelResId: Int,
    val color: Color
) {
    ACTIVE(R.string.tracker_active, Color.Companion.Green),
    WAITING(R.string.tracker_waiting, Color.Companion.Yellow),
    INACTIVE(R.string.tracker_inactive, Color.Companion.Red);

    @Composable
    fun getLabel(): String {
        return stringResource(id = labelResId)
    }
}