package com.gribansky.opentracker.ui

import android.app.Application
import com.gribansky.opentracker.core.TrackerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewServiceViewModel : ServiceViewModel(Application()) {
    private val _previewUiOverView = MutableStateFlow(
        TrackerState(
            serviceLastStartTime = System.currentTimeMillis(),
            isForeground = true
        )
    )
    override val uiOverView: StateFlow<TrackerState> = _previewUiOverView

    override fun bindService() {
        // Do nothing in preview
    }
}