package com.gribansky.opentracker.ui.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.gribansky.opentracker.Overview
import com.gribansky.opentracker.Permissions


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    navController: NavController,
) {
    val context = LocalContext.current

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
                    navigateToOverview(navController)
                }
            } ?: run {
                navigateToOverview(navController)
            }
        }
    }


    LaunchedEffect(Unit) {
        if (foregroundPermissionsState.allPermissionsGranted &&
            (backgroundLocationState?.status?.isGranted != false)) {
            navigateToOverview(navController)
        }
    }



    if (!foregroundPermissionsState.allPermissionsGranted ||
        (backgroundLocationState?.status?.isGranted == false)) {
        PermissionRequestUI(
            onRequestPermissions = {
                if (!foregroundPermissionsState.allPermissionsGranted) {
                    foregroundPermissionsState.launchMultiplePermissionRequest()
                } else {
                    backgroundLocationState?.launchPermissionRequest()
                }
            },
            onOpenSettings = { openAppSettings(context) }
        )
    }
}



@Composable
fun PermissionRequestUI(
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Для работы приложения необходимы следующие разрешения:")
        Spacer(modifier = Modifier.height(16.dp))
        Text("- Доступ к точной геолокации")
        Text("- Доступ к приблизительной геолокации")

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


// Вспомогательные функции
fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}


private fun navigateToOverview(navController: NavController) {
    if (navController.currentDestination?.route != Overview.route) {
        navController.navigate(Overview.route) {
            popUpTo(Permissions.route) { inclusive = true }
        }
    }
}