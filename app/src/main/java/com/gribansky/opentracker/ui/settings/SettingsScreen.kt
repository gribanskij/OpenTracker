package com.gribansky.opentracker.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gribansky.opentracker.data.UserPreferences
import com.gribansky.opentracker.ui.ServiceViewModel
import com.gribansky.opentracker.ui.theme.TrackerTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModelStoreOwner: ViewModelStoreOwner,
) {
    val viewModel: SettingsViewModel = viewModel(viewModelStoreOwner)
    val preferences by viewModel.userPreferences.collectAsState()
    Settings(
        modifier = modifier.fillMaxSize(),
        preferences = preferences,
        userNameChanged = viewModel::updateUsername,
        userPasswordChanged = viewModel::updatePassword,
        serverAddressChanged = viewModel::updateServerAddress,
        workTimeChanged = viewModel::updateUseWorkTime
    )
}

@Composable
fun Settings(
    modifier:Modifier = Modifier,
    preferences: UserPreferences,
    userNameChanged: (String) -> Unit,
    userPasswordChanged: (String) -> Unit,
    serverAddressChanged: (String) -> Unit,
    workTimeChanged: (Boolean) -> Unit
) {
    val typography = MaterialTheme.typography
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = preferences.username,
            onValueChange = userNameChanged,
            label = { Text("Имя пользователя") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = preferences.password,
            onValueChange = userPasswordChanged,
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
        )

        OutlinedTextField(
            value = preferences.serverAddress,
            onValueChange = serverAddressChanged,
            label = { Text("Адрес сервера") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onBackground,
                text = "Использовать рабочее время")
            Switch(
                checked = preferences.useWorkTime,
                onCheckedChange = workTimeChanged
            )
        }
    }
}
@Preview (uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SettingsPreview() {

    TrackerTheme {
        Settings(
            preferences = UserPreferences(),
            userNameChanged = {},
            userPasswordChanged = {},
            serverAddressChanged = {},
            workTimeChanged = {}
        )
    }
}
