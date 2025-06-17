package com.gribansky.opentracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.gribansky.opentracker.ui.MainScreen
import com.gribansky.opentracker.ui.permissions.BatteryOptScreen
import com.gribansky.opentracker.ui.permissions.PermissionScreen
import com.gribansky.opentracker.ui.theme.TrackerTheme

class TrackerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackerApp()
        }
    }
}

@Composable
fun TrackerApp() {
    TrackerTheme {
        val context = LocalContext.current
        var showPermissionScreen by remember {
            mutableStateOf(!context.hasAllPermissions())
        }

        var showBatteryOptScreen by remember {
            mutableStateOf(context.isBatteryOptimizationEnabled())
        }


        when {
            showPermissionScreen -> {
                PermissionScreen(
                    onDismiss = { showPermissionScreen = false },
                    onOpenSettings = { context.openAppSettings() }
                )
            }

            showBatteryOptScreen -> {
                BatteryOptScreen (
                    onDismiss = { showBatteryOptScreen = false },
                    onOpenBatteryOptSettings = { context.openBatteryOptimizationSettings() }
                )
            }

            else -> {
                MainScreen()
            }
        }
    }
}

fun Context.hasAllPermissions(): Boolean {
    val requiredPermissions = listOfNotNull(
        Manifest.permission.ACCESS_FINE_LOCATION,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.ACCESS_BACKGROUND_LOCATION else null
    )

    return requiredPermissions.all { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}

// Вспомогательные функции
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}

// Открывает системные настройки энергосбережения для всех приложений
fun Context.openBatteryOptimizationSettings() {
    val intent = Intent().apply {
        action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
    }
    startActivity(intent)
}

fun Context.isBatteryOptimizationEnabled(): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    return !powerManager.isIgnoringBatteryOptimizations(packageName)
}
