package com.gribansky.opentracker.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.SystemClock
import androidx.core.app.ServiceCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import com.gribansky.opentracker.core.log.FileSaver
import com.gribansky.opentracker.core.log.ILocation
import com.gribansky.opentracker.core.log.ILogManager
import com.gribansky.opentracker.core.log.LocationManager
import com.gribansky.opentracker.core.log.LogManager
import com.gribansky.opentracker.core.log.LogResult
import com.gribansky.opentracker.core.log.NetSender
import com.gribansky.opentracker.core.log.PositionData
import com.gribansky.opentracker.core.log.PositionDataLog
import com.gribansky.opentracker.data.dataStore
import com.gribansky.opentracker.ui.TrackerState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

const val TRACKER_TIMER_ACTION = "intent.action.TIMER_FIRED"
const val TRACKER_CLIENT_BIND = "intent.action.CLIENT_BIND"

private const val TRACKER_LOCATION_POINT_INTERVAL = 5 * 60 * 1000L // 5 минут
private const val HISTORY_WINDOW = 60
private const val WAKE_LOCK_NAME = "OpenTracker:TrackerService"
private const val TRACK_TIMER_CODE = 100
private const val WAKE_LOCK_TIMEOUT = 5000L
private const val LONG_WAKE_LOCK_TIMEOUT = 180000L

class TrackerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate)
    private val _trackerHistory = MutableStateFlow(emptyList<PositionData>())
    private val _trackerState = MutableStateFlow(TrackerState())
    private val commands = MutableSharedFlow<String>(
        extraBufferCapacity = 5,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    private lateinit var  timeManager:TimeManager
    private val binder = LocalBinder()
    private val trackerHist = ArrayDeque<PositionData>(HISTORY_WINDOW)
    private val events = mutableListOf<PositionDataLog>()

    private val lock: WakeLock by lazy {
        (getSystemService(POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME).apply {
                setReferenceCounted(true)
            }
        }
    }

    private val handler = CoroutineExceptionHandler { _, ex ->
        ex.printStackTrace()
        addLogToHistory("COLLECT_ERROR", ex.message ?: ex.toString())
    }

    // Expose state flows as read-only versions
    val trackerHistory: StateFlow<List<PositionData>> = _trackerHistory.asStateFlow()
    val trackerState: StateFlow<TrackerState> = _trackerState.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock(WAKE_LOCK_TIMEOUT)
        createNotificationChannel(this)

        timeManager = TimeManager(this.dataStore)

        val logManager = LogManager(
            locationProvider = LocationManager(LocationServices.getFusedLocationProviderClient(this)),
            saver = FileSaver(),
            sender = NetSender()
        )

        serviceScope.launch(handler) {
            commands.collect { startLogging(logManager) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        acquireWakeLock(WAKE_LOCK_TIMEOUT)
        handleAction(intent?.action ?: "")
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder
    override fun onUnbind(intent: Intent?): Boolean = true
    override fun onRebind(intent: Intent?) = super.onRebind(intent)

    override fun onDestroy() {
        serviceScope.cancel("Service is destroying...")
        releaseWakeLock()
        super.onDestroy()
    }

    private suspend fun startLogging(logManager: ILogManager) {
        if (!acquireWakeLock(LONG_WAKE_LOCK_TIMEOUT)) return

        try {
            val serviceEvents = events.toList().map { it.getDataInString() }
            events.clear()

            val result = logManager.startLogCollect(getPathToLog(), serviceEvents, false)
            updateLogState(result)
            result.collectedPoints.forEach(::updateHistory)
        } catch (ex: Exception) {
            ex.printStackTrace()
            addLogToHistory("LOGGING_ERROR", ex.message ?: ex.toString())
        } finally {
            releaseWakeLock()
        }
    }

    private fun handleAction(action: String) = when (action) {
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
        Intent.ACTION_DATE_CHANGED,
        Intent.ACTION_TIME_CHANGED -> handleSystemAction(action)
        TRACKER_CLIENT_BIND -> handleClientBind()
        TRACKER_TIMER_ACTION -> handleTimerAction()
        else -> Unit
    }

    private fun handleSystemAction(action: String) {
        val sTime = restartTimer()
        updateStartTime(sTime)

        if (action != Intent.ACTION_BOOT_COMPLETED && !timeManager.isInWrkTimeNow()) {
            stopForeground()
            updateForegroundState(false)
        }

        if (action == Intent.ACTION_DATE_CHANGED || action == Intent.ACTION_TIME_CHANGED) {
            addLogToHistory(action.substringAfterLast('.'), "changed")
        }
    }

    private fun handleClientBind() {
        if (_trackerState.value.serviceLastStartTime == null) {
            updateStartTime(restartTimer())
            addLogToHistory("CLIENT_BIND", "restart timer")
        }
    }

    private fun handleTimerAction() {
        if (timeManager.isInWrkTimeNow()) {
            if (!_trackerState.value.isForeground) {
                startForeground()
                updateForegroundState(true)
                addLogToHistory("TIMER_ACTION", "start foreground")
            }
            if (_trackerState.value.serviceLastStartTime == null) {
                updateStartTime(System.currentTimeMillis())
                addLogToHistory("TIMER_ACTION", "last start time updated")
            }
            addLogToHistory("TIMER_ACTION", "start collecting GPS")
            commands.tryEmit("start")
        } else {
            updateStartTime(restartTimer())
            addLogToHistory("TIMER_ACTION", "stop foreground")
            updateForegroundState(false)
            stopForeground()
        }
    }

    private fun updateStartTime(sTime: Long) {
        _trackerState.update { it.copy(serviceLastStartTime = sTime) }
    }

    private fun updateForegroundState(isForeground: Boolean) {
        _trackerState.update { it.copy(isForeground = isForeground) }
    }

    private fun restartTimer(): Long = timeManager.getNextTimePoint().also { restartTimer(it) }

    private fun restartTimer(futureTriggerTime: Long, interval: Long = TRACKER_LOCATION_POINT_INTERVAL) {
        val intent = Intent(this, TrackerReceiver::class.java).apply {
            action = TRACKER_TIMER_ACTION
        }

        val tTime = SystemClock.elapsedRealtime() + futureTriggerTime - System.currentTimeMillis()
        val pIntent = PendingIntent.getBroadcast(
            this, TRACK_TIMER_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        (getSystemService(ALARM_SERVICE) as AlarmManager).setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP, tTime, interval, pIntent
        )
    }

    private fun startForeground() {
        val notification = getNotification(this)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            0
        }
        ServiceCompat.startForeground(
            this,
            FOREGROUND_NOTIFICATION_ID,
            notification,
            type
        )
    }

    private fun stopForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun updateLogState(logResult: LogResult) {


        _trackerState.update {
            it.copy(
                gpsLastTimeReceived = if (logResult.collectedPoints.isNotEmpty())logResult.time else it.gpsLastTimeReceived,
                gsmLastTimeReceived = null,
                packetsSentLastTime = if (logResult.sent != null && logResult.sent > 0 ) logResult.time else it.packetsSentLastTime,
                packetsReadyLastTime = if (logResult.ready != null && logResult.ready > 0 ) logResult.time else it.packetsReadyLastTime,
                logTime = logResult.time,
                packetsReady = logResult.ready,
                packetsSent = logResult.sent
            )
        }
    }

    private fun updateHistory(log: PositionData) {
        if (trackerHist.size == HISTORY_WINDOW) {
            trackerHist.removeLast()
        }
        trackerHist.addFirst(log)
        _trackerHistory.update { trackerHist.toList() }
    }

    private fun addLogToHistory(tag: String, mes: String) {
        val logEvent = PositionDataLog(logTag = tag, logMessage = mes)
        events.add(logEvent)
        updateHistory(logEvent)
    }

    private fun getPathToLog(): String {
        return File(filesDir, "trkLog").apply {
            if (!exists()) mkdir()
        }.absolutePath
    }

    private fun acquireWakeLock(timeout: Long): Boolean {
        return try {
            lock.acquire(timeout)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun releaseWakeLock() {
        if (lock.isHeld) {
            lock.release()
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): TrackerService = this@TrackerService
    }
}