package com.gribansky.opentracker.ui.history

import android.app.Application
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gribansky.opentracker.core.log.PositionData
import com.gribansky.opentracker.core.log.PositionDataLog
import com.gribansky.opentracker.core.log.PositionGpsData
import com.gribansky.opentracker.core.log.PositionGsmData
import com.gribansky.opentracker.ui.ServiceViewModel
import com.gribansky.opentracker.ui.ServiceViewModelFactory
import com.gribansky.opentracker.ui.components.GSMRow
import com.gribansky.opentracker.ui.components.GpsRow
import com.gribansky.opentracker.ui.components.LogRow
import com.gribansky.opentracker.ui.components.formatDateTime
import com.gribansky.opentracker.ui.theme.TrackerTheme
import java.sql.Date


@Composable
fun HistoryScreen(
    viewModelStoreOwner: ViewModelStoreOwner,

) {
    val context = LocalContext.current
    val viewModel: ServiceViewModel = viewModel(
        viewModelStoreOwner= viewModelStoreOwner,
        factory = ServiceViewModelFactory(context.applicationContext as Application)
    )
    val trackerLog by viewModel.uiHistory.collectAsStateWithLifecycle()
    TrackerHistory(history = trackerLog)
}

@Composable
fun TrackerHistory(
    modifier: Modifier = Modifier,
    history: List<PositionData>,
) {

    LazyColumn (
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        items(history) { item ->


            when (item) {
                is PositionGpsData -> {

                    val dateTime = formatDateTime(Date(item.gpsLocation.time))
                    val mes = "lt:${item.gpsLocation.latitude}, ln:${item.gpsLocation.longitude}"
                    GpsRow(
                        onDate = dateTime,
                        color = Color.Green,
                        message = mes
                    )
                }

                is PositionGsmData-> {
                    val dateTime = formatDateTime(Date(item.eventDate))
                    val mes = "GSM point"
                    GSMRow(
                        onDate = dateTime,
                        color = Color.Green,
                        message = mes
                    )
                }

                is PositionDataLog -> {
                    val dateTime = formatDateTime(Date(item.eventDate))
                    val mes = "${item.logTag}:${item.logMessage}"
                    val color = if (mes.contains("exception", ignoreCase = true)) Color.Red else Color.Yellow
                    LogRow(
                        onDate = dateTime,
                        message = mes,
                        color = color
                    )

                }
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HistoryScreenPreview() {

    val log = listOf(
        PositionDataLog (logTag = "test", logMessage = "test"),
        PositionDataLog (logTag = "exception", logMessage = "ERROR"))
    TrackerTheme {
        TrackerHistory (history = log)
    }
}
