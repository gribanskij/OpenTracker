package com.gribansky.opentracker.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gribansky.opentracker.ui.ServiceViewModel
import com.gribansky.opentracker.ui.components.GpsRow
import com.gribansky.opentracker.ui.components.PacketRow
import com.gribansky.opentracker.ui.components.TrackerAlertDialog
import com.gribansky.opentracker.ui.components.TrackerDivider
import java.util.Date
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.CompositionLocalProvider
import com.gribansky.opentracker.core.TrackerStatus
import com.gribansky.opentracker.ui.PreviewServiceViewModel
import com.gribansky.opentracker.core.TrackerState
import com.gribansky.opentracker.ui.components.GSMRow
import com.gribansky.opentracker.ui.components.formatDateTime
import com.gribansky.opentracker.ui.theme.TrackerTheme

@Composable
fun OverviewScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    onAccountClick: (String) -> Unit = {},
) {
    val viewModel: ServiceViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = (viewModelStoreOwner as? PreviewViewModelOwner)?.factory
    )
    val uiState by viewModel.uiOverView.collectAsStateWithLifecycle()

    // Автоматическое подключение при первом отображении
    LaunchedEffect(Unit) {
        viewModel.bindService()
    }
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .semantics { contentDescription = "Overview Screen" }
    ) {
        //AlertCard()
        Spacer(Modifier.height(TrackerDefaultPadding))
        OverviewScreenCard (
            uiState = uiState,
            onClickSendAll = { viewModel.sendAll() },
        )
    }
}


@Composable
private fun AlertCard() {
    var showDialog by remember { mutableStateOf(false) }
    val alertMessage = "Heads up, you've used up 90% of your Shopping budget for this month."

    if (showDialog) {
        TrackerAlertDialog(
            onDismiss = {
                showDialog = false
            },
            bodyText = alertMessage,
            buttonText = "Dismiss".uppercase(Locale.getDefault())
        )
    }
    Card {
        Column {
            AlertHeader {
                showDialog = true
            }
            TrackerDivider(
                modifier = Modifier.padding(start = TrackerDefaultPadding, end = TrackerDefaultPadding)
            )
            AlertItem(alertMessage)
        }
    }
}

@Composable
private fun AlertHeader(onClickSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(TrackerDefaultPadding)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Alerts",
            style = typography.subtitle2,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        TextButton(
            onClick = onClickSeeAll,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text(
                text = "SEE ALL",
                style =typography.button,
            )
        }
    }
}

@Composable
private fun AlertItem(message: String) {
    Row(
        modifier = Modifier
            .padding(TrackerDefaultPadding)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            style = typography.body2,
            modifier = Modifier.weight(1f),
            text = message
        )
        IconButton(
            onClick = {},
            modifier = Modifier
                .align(Alignment.Top)
                .clearAndSetSemantics {}
        ) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
        }
    }
}


@Composable
private fun OverviewScreenCard(
    uiState: TrackerState,
    onClickSendAll: () -> Unit,
) {

    val status = when {
        uiState.isForeground -> TrackerStatus.ACTIVE
        uiState.isForeground == false && uiState.serviceLastStartTime != null -> TrackerStatus.WAITING
        else -> TrackerStatus.INACTIVE
    }
    val trackerStartTime = uiState.serviceLastStartTime?.let { formatDateTime(Date(it)) }?:"-"
    val gpsTime = uiState.gpsLastTimeReceived?.let { formatDateTime(Date(it)) }?:"-"
    val gsmTime = uiState.gsmLastTimeReceived?.let { formatDateTime(Date(it)) }?:"-"
    val packetSent = uiState.packetsSentLastTime?.let { formatDateTime(Date(it)) }?:"-"
    val packetReady = uiState.packetsReadyLastTime?.let { formatDateTime(Date(it)) }?:"-"
    val packetTime = if (uiState.packetsSent !=null && uiState.packetsSent > 0 ) packetSent else packetReady
    val packetWhat = if (uiState.packetsSent !=null && uiState.packetsSent > 0 ) "Отправлено (${uiState.packetsSent})"
     else "Готовы к отправке (${uiState.packetsReady ?: "-"})"


    Card {
        Column {
            Row(
                modifier = Modifier.padding(TrackerDefaultPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text (
                        text = status.label,
                        style = typography.body2,
                        color = MaterialTheme.colors.onBackground
                    )

                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = trackerStartTime,
                            style = typography.subtitle1,
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(status.color, CircleShape)
                )
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF005D57))
            )
            Column(Modifier.padding(start = 16.dp, top = 4.dp, end = 8.dp)) {

                GpsRow(onDate = gpsTime, color = Color.Green)
                GSMRow(onDate = gsmTime, color = Color.Green)
                PacketRow(
                    message = packetWhat,
                    onDate = packetTime,
                    color = Color.Green
                )

                SendAllButton(
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "send now "
                    },
                    onClick = onClickSendAll,
                )
            }
        }
    }
}


@Composable
private fun SendAllButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .fillMaxWidth()
    ) {

        Text("Отправить данные сейчас")
    }
}

private val TrackerDefaultPadding = 12.dp




private class PreviewViewModelOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
    
    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return PreviewServiceViewModel() as T
        }
    }
}





@Preview(showBackground = true)
@Composable
fun OverviewScreenPreview() {
    TrackerTheme {
        OverviewScreen(
            viewModelStoreOwner = PreviewViewModelOwner(),
            onAccountClick = {}
        )
    }
}


