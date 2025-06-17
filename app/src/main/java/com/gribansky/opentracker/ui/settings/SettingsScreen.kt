package com.gribansky.opentracker.ui.settings

import android.app.Application
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gribansky.opentracker.R
import com.gribansky.opentracker.data.UserPreferences
import com.gribansky.opentracker.ui.theme.TrackerTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModelStoreOwner: ViewModelStoreOwner,
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = SettingsViewModelFactory(context.applicationContext as Application)
    )
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
    modifier: Modifier = Modifier,
    preferences: UserPreferences,
    userNameChanged: (String) -> Unit,
    userPasswordChanged: (String) -> Unit,
    serverAddressChanged: (String) -> Unit,
    workTimeChanged: (Boolean) -> Unit
) {
    var username by remember { mutableStateOf(preferences.username) }
    var password by remember { mutableStateOf(preferences.password) }
    var serverAddress by remember { mutableStateOf(preferences.serverAddress) }

    LaunchedEffect(preferences) {
        username = preferences.username
        password = preferences.password
        serverAddress = preferences.serverAddress
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(R.string.username)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { userNameChanged(username) }
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { userPasswordChanged(password) }
            ),
            singleLine = true,
        )

        OutlinedTextField(
            value = serverAddress,
            onValueChange = { serverAddress = it },
            label = { Text(stringResource(R.string.server_address)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { serverAddressChanged(serverAddress) }
            ),
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
                text = stringResource(R.string.use_work_time))
            Switch(
                checked = preferences.useWorkTime,
                onCheckedChange = workTimeChanged
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
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
