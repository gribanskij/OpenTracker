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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File


const val TRACKER_TIMER_ACTION = "intent.action.TIMER_FIRED"
const val TRACKER_CLIENT_BIND = "intent.action.CLIENT_BIND"
const val TRACKER_LOCATION_POINT_INTERVAL = 5 * 60 * 1000L //5 минут
private const val HISTORY_WINDOW = 60


private const val WAKE_LOCK_NAME = "OpenTracker:TrackerService"
private const val TAG = "TrackerService"
private const val TRACK_TIMER_CODE = 100


class TrackerService : Service() {


    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate)

    private val _trackerHistory = MutableStateFlow(emptyList<PositionData>())
    val trackerHistory: StateFlow<List<PositionData>> = _trackerHistory.asStateFlow()

    private val _trackerState = MutableStateFlow(TrackerState())
    val trackerState: StateFlow<TrackerState> = _trackerState.asStateFlow()

    private val commands = MutableSharedFlow<String>(
        extraBufferCapacity = 5,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )


    private val timeManager = TimeManager()
    private val startMode: Int = START_NOT_STICKY
    private val binder: IBinder = LocalBinder()
    private val allowRebind = true
    private val trackerHist = ArrayDeque<PositionData>()

    private val events = mutableListOf<String>()


    private val prefManager: IPrefManager by lazy {
        SharePrefManager(PreferenceManager.getDefaultSharedPreferences(this))
    }


    private val lock: WakeLock by lazy {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME).apply {
            setReferenceCounted(true)
        }
    }


    private val handler = CoroutineExceptionHandler { _, exception ->

    }


    override fun onCreate() {
        super.onCreate()
        lock.acquire(5000)

        val logManager:ILogManager = LogManager(
            locationProvider = LocationManager(LocationServices.getFusedLocationProviderClient(this)),
            saver = FileSaver(),
            sender = NetSender()
        )

        serviceScope.launch(handler) {

            try {
                commands.collect {


                    try {
                        lock.acquire(180000)
                        val result = logManager.startLogCollect(getPathToLog(), buildList{addAll(events)},false)
                        updateLogState(result)
                        result.points.forEach { updateHistory(it) }
                        lock.release()

                    }catch (ex:Exception){
                        ex.printStackTrace()
                    }

                }

            } catch (ex:Exception){

                ex.printStackTrace()

            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lock.acquire(5000)
        handleAction(intent?.action ?: "")
        return startMode
    }


    override fun onBind(intent: Intent?): IBinder {
        return binder

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return allowRebind
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel("Service is destroying...")
        while (lock.isHeld) {
            lock.release()
        }

    }


    private fun handleAction(action: String) {
        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED -> handleSystemAction(action)
            TRACKER_CLIENT_BIND -> handleClientBind()
            TRACKER_TIMER_ACTION -> handleTimerAction()
        }
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

    private fun updateStartTime(sTime: Long) =
        _trackerState.update { it.copy(serviceLastStartTime = sTime) }


    private fun updateForegroundState(isForeground: Boolean) =
        _trackerState.update { it.copy(isForeground = isForeground) }

    private fun restartTimer(): Long =
        timeManager.getNextTimePoint().also { restartTimer(it) }

    private fun restartTimer(futureTriggerTime: Long, interval: Long = TRACKER_LOCATION_POINT_INTERVAL) {
        val intent = Intent(this, TrackerReceiver::class.java).apply { action = TRACKER_TIMER_ACTION }
        val tTime = SystemClock.elapsedRealtime() + futureTriggerTime - System.currentTimeMillis()
        PendingIntent.getBroadcast(
            this, TRACK_TIMER_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ).let { pIntent ->
            (getSystemService(ALARM_SERVICE) as AlarmManager).setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, tTime, interval, pIntent)
        }
    }

    private fun startForeground() {

        val notification = getNotification(this)
        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
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
        val currentState = _trackerState.value
        val newState = currentState.copy(
            logTime = logResult.time,
            packetsReady = logResult.ready,
            packetsSent = logResult.sent
        )
        _trackerState.update { newState }
    }


    private fun updateHistory(log: PositionData) {
        if (trackerHist.count() == HISTORY_WINDOW) trackerHist.removeLastOrNull()
        trackerHist.addFirst(log)
        _trackerHistory.update { trackerHist.toList() }
    }

    inner class LocalBinder : Binder() {
        fun getService(): TrackerService = this@TrackerService
    }

    private fun addLogToHistory(tag: String, mes: String) {

        updateHistory(
            PositionDataLog(
                logTag = tag,
                logMessage = mes
            )
        )
    }

    private fun getPathToLog(): String {
        val logPath = File(filesDir, "trkLog")
        if (!logPath.exists()) logPath.mkdir()
        return logPath.absolutePath
    }
}
