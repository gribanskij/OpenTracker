package com.gribansky.opentracker.ui.permissions

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.gribansky.opentracker.ui.theme.TrackerTheme


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {

    val permissions = listOfNotNull(
        Manifest.permission.ACCESS_FINE_LOCATION,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else null
    )

    val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    } else null

    val foregroundPermissionsState = rememberMultiplePermissionsState(permissions)
    val backgroundLocationState = backgroundLocationPermission?.let {
        rememberPermissionState(it)
    }


    LaunchedEffect(foregroundPermissionsState.allPermissionsGranted) {
        if (foregroundPermissionsState.allPermissionsGranted) {
            backgroundLocationState?.let {
                if (it.status != PermissionStatus.Granted) {
                    it.launchPermissionRequest()
                } else {
                    onDismiss()
                }
            } ?: run {
                onDismiss()
            }
        }
    }


    LaunchedEffect(Unit) {
        if (foregroundPermissionsState.allPermissionsGranted &&
            (backgroundLocationState?.status?.isGranted != false)
        ) {
            onDismiss()
        }
    }

    if (!foregroundPermissionsState.allPermissionsGranted ||
        (backgroundLocationState?.status?.isGranted == false)
    ) {
        PermissionRequestUI(
            onRequestPermissions = {
                if (!foregroundPermissionsState.allPermissionsGranted) {
                    foregroundPermissionsState.launchMultiplePermissionRequest()
                } else {
                    backgroundLocationState?.launchPermissionRequest()
                }
            },
            onOpenSettings = onOpenSettings
        )
    } else {
        onDismiss()
    }
}


@Composable
fun PermissionRequestUI(
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Для работы приложения необходимы следующие разрешения:",
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text("- Доступ к точной геолокации")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Text("- Доступ к геолокации в фоне")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Text("- Разрешение на уведомления")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onRequestPermissions) {
                Text("Запросить разрешения")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onOpenSettings) {
                Text("Открыть настройки")
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PermissionRequestUIPreview() {
    TrackerTheme {
        PermissionRequestUI(
            {}, {}
        )
    }
}




