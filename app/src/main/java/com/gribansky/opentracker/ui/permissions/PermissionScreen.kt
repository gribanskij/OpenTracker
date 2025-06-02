package com.gribansky.opentracker.ui.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    navController: NavController,
) {
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Проверяем статус разрешения при запуске
    LaunchedEffect(Unit) {
        if (locationPermissionState.status.isGranted) {
            navController.navigate("main") { popUpTo("permission") }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (locationPermissionState.status) {
            is PermissionStatus.Granted -> {
                Text("Разрешение получено")
                Button(onClick = { navController.navigate("main") }) {
                    Text("Продолжить")
                }
            }
            is PermissionStatus.Denied -> {
                Text("Для работы приложения нужен доступ к геолокации")
                Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                    Text("Запросить разрешение")
                }
            }
        }
    }
}