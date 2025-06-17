package com.gribansky.opentracker.ui.overview

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gribansky.opentracker.R
import com.gribansky.opentracker.ui.ServiceViewModel
import com.gribansky.opentracker.ui.components.GpsRow
import com.gribansky.opentracker.ui.components.PacketRow
import java.util.Date
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.gribansky.opentracker.core.TrackerStatus
import com.gribansky.opentracker.core.TrackerState
import com.gribansky.opentracker.ui.ServiceViewModelFactory
import com.gribansky.opentracker.ui.components.GSMRow
import com.gribansky.opentracker.ui.components.formatDateTime
import com.gribansky.opentracker.ui.theme.TrackerTheme

@Composable
fun OverviewScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
) {
    val context = LocalContext.current
    val viewModel: ServiceViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = ServiceViewModelFactory(context.applicationContext as Application)
    )
    val uiState by viewModel.uiOverView.collectAsStateWithLifecycle()

    // Автоматическое подключение при первом отображении
    LaunchedEffect(Unit) {
        viewModel.bindService()
    }
    Overview(
        uiState = uiState,
        onSendAllClick = viewModel::sendAll
    )
}

@Composable
fun Overview(
    modifier: Modifier = Modifier,
    uiState: TrackerState,
    onSendAllClick: () -> Unit = {},
    ){

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .semantics { contentDescription = "Overview Screen" }
    ) {
        Spacer(Modifier.height(TrackerDefaultPadding))
        OverviewScreenCard (
            uiState = uiState,
            onClickSendAll = onSendAllClick,
        )
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
                        text = status.getLabel(),
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
        Text(stringResource(R.string.send_data_now))
    }
}

private val TrackerDefaultPadding = 12.dp



@Preview(showBackground = true)
@Composable
fun OverviewScreenPreview() {
    TrackerTheme {
        Overview(
            uiState = TrackerState()
        )
    }
}

