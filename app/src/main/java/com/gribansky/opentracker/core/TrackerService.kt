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
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


const val TRACKER_TIMER_ACTION = "intent.action.TIMER_FIRED"
const val TRACKER_CLIENT_BIND = "intent.action.CLIENT_BIND"
const val TRACKER_LOCATION_POINT_INTERVAL = 5 * 60 * 1000L //5 минут

private const val HISTORY_WINDOW = 60


private const val WAKE_LOCK_NAME = "OpenTracker:TrackerService"
private const val TAG = "TrackerService"
private const val TRACK_TIMER_CODE = 100


class TrackerService : Service() {


    private val _trackerHistory = MutableStateFlow(emptyList<PositionData>())
    val trackerHistory: StateFlow<List<PositionData>> = _trackerHistory.asStateFlow()

    private val _trackerState = MutableStateFlow(TrackerState())
    val trackerState: StateFlow<TrackerState> = _trackerState.asStateFlow()

    private val serviceScope = MainScope()
    private val timeManager = TimeManager()
    private val startMode: Int = START_NOT_STICKY
    private val binder: IBinder = LocalBinder()
    private val allowRebind = true

    private val trackerHist = ArrayDeque<PositionData>()

    private var locationProvider:ILocation? = null

    private val prefManager: IPrefManager by lazy {
        SharePrefManager(PreferenceManager.getDefaultSharedPreferences(this))
    }


    private val lock: WakeLock by lazy {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME).apply {
            setReferenceCounted(true)
        }
    }


    override fun onCreate() {
        super.onCreate()
        lock.acquire(5000)
        val fusedLocationManager = LocationServices.getFusedLocationProviderClient(this)
        //val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        locationProvider = LocationManager(fusedLocationManager,::positionsReady)
       // _trackerState.update { prefManager.state }

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
        locationProvider?.stop()
        //prefManager.state = _trackerState.value

        while (lock.isHeld) {
            lock.release()
        }

    }



    private fun handleAction(action: String) {

        when (action) {

            Intent.ACTION_BOOT_COMPLETED -> {
                val sTime = restartTimer()
                updateStartTime(sTime)

                addLogToHistory("SYS","app restarted")

            }

            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val sTime = restartTimer()
                updateStartTime(sTime)
                if (!timeManager.isInWrkTimeNow()) {
                    stopForeground()
                    updateForegroundState(false)
                }
            }

            Intent.ACTION_DATE_CHANGED -> {
                val sTime = restartTimer()
                updateStartTime(sTime)
                if (!timeManager.isInWrkTimeNow()) {
                    stopForeground()
                    updateForegroundState(false)
                }

                addLogToHistory("DATE","date changed")
            }

            Intent.ACTION_TIME_CHANGED -> {
                val sTime = restartTimer()
                updateStartTime(sTime)
                if (!timeManager.isInWrkTimeNow()) {
                    stopForeground()
                    updateForegroundState(false)
                }

                addLogToHistory("TIME","time changed")
            }

            TRACKER_CLIENT_BIND -> {

                val currentState = _trackerState.value
                if (currentState.serviceLastStartTime == null) {
                    val sTime = restartTimer()
                    updateStartTime(sTime)
                    addLogToHistory("CLIENT_BIND","restart timer")
                }

            }

            TRACKER_TIMER_ACTION -> {
                lock.acquire(5000)

                if (timeManager.isInWrkTimeNow()) {

                    val currentState = _trackerState.value
                    if (!currentState.isForeground) {
                        startForeground()
                        updateForegroundState(true)
                        addLogToHistory("TIMER_ACTION","start foreground")

                    }
                    if (currentState.serviceLastStartTime == null){
                        updateStartTime(System.currentTimeMillis())
                        addLogToHistory("TIMER_ACTION","last start time updated")
                    }

                    lock.acquire(90000)
                    locationProvider?.start()
                    addLogToHistory("TIMER_ACTION","start collecting GPS")


                } else {
                    val sTime = restartTimer()
                    updateStartTime(sTime)
                    addLogToHistory("TIMER_ACTION","stop foreground")
                    updateForegroundState(false)
                    stopForeground()
                }

            }

            else -> {

            }
        }
    }

    private fun updateStartTime(sTime: Long) {
        val currentState = _trackerState.value
        val newState = currentState.copy(serviceLastStartTime = sTime)
        _trackerState.update { newState }
    }

    private fun updateForegroundState(isForeground: Boolean) {
        val currentState = _trackerState.value
        val newState = currentState.copy(isForeground = isForeground)
        _trackerState.update { newState }
    }

    private fun restartTimer(): Long {
        val sTime = timeManager.getNextTimePoint()
        restartTimer(sTime)
        return sTime
    }

    private fun restartTimer(
        futureTriggerTime: Long,
        interval: Long = TRACKER_LOCATION_POINT_INTERVAL
    ) {
        val intent = Intent(this, TrackerReceiver::class.java).apply {
            action = TRACKER_TIMER_ACTION
        }
        val tTime = SystemClock.elapsedRealtime() + futureTriggerTime - System.currentTimeMillis()
        val pIntent = PendingIntent.getBroadcast(
            this,
            TRACK_TIMER_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (getSystemService(ALARM_SERVICE) as AlarmManager).apply {
            setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, tTime, interval, pIntent)
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


    private fun positionsReady(pos:List<PositionData>){
        val p = pos.ifEmpty {
            listOf(PositionDataLog(
                logTag = "GPS reciver:",
                logMessage = "no points collected"
            ))
        }
        p.forEach { updateHistory(it) }


        lock.release()
    }


    private fun updateHistory(log:PositionData){
        if (trackerHist.count() == HISTORY_WINDOW) trackerHist.removeLastOrNull()
        trackerHist.addFirst(log)
        _trackerHistory.update {trackerHist.toList() }
    }

    inner class LocalBinder : Binder() {
        fun getService(): TrackerService = this@TrackerService
    }

    private fun addLogToHistory(tag:String,mes:String){

        updateHistory(PositionDataLog(
            logTag = tag,
            logMessage = mes
        ))


    }
}
