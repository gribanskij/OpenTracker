package com.gribansky.opentracker.ui


import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gribansky.opentracker.core.TRACKER_CLIENT_BIND
import com.gribansky.opentracker.core.TrackerService
import com.gribansky.opentracker.core.TrackerState
import com.gribansky.opentracker.core.log.PositionData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServiceViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiOverView = MutableStateFlow(TrackerState())
    val uiOverView: StateFlow<TrackerState> = _uiOverView.asStateFlow()

    private val _uiHistory = MutableStateFlow(emptyList<PositionData>())
    val uiHistory: StateFlow<List<PositionData>> = _uiHistory.asStateFlow()

    private var boundService: TrackerService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TrackerService.LocalBinder
            boundService = binder.getService()
            isBound = true
            
            // Подписываемся на изменения состояния сервиса
            viewModelScope.launch {
                boundService?.trackerState?.collect { trackerState ->
                    _uiOverView.value = trackerState
                }
            }

            viewModelScope.launch {
                boundService?.trackerHistory?.collect{ history->
                    _uiHistory.value = history

                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            boundService = null
        }
    }

    fun bindService() {
        val intent = Intent(getApplication(), TrackerService::class.java).apply {
            action = TRACKER_CLIENT_BIND
        }
        getApplication<Application>().startService(intent)
        getApplication<Application>().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        if (isBound) {
            getApplication<Application>().unbindService(connection)
            isBound = false
        }
    }

    fun sendAll() {
    }


    override fun onCleared() {
        super.onCleared()
        unbindService()
    }
}

class ServiceViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceViewModel::class.java)) {
            return ServiceViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

