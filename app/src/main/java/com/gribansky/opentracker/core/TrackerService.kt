package com.gribansky.opentracker.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


const val TRACKER_TIMER_ACTION = "intent.action.TIMER_FIRED"
const val TRACKER_CLIENT_BIND = "intent.action.CLIENT_BIND"
const val TRACKER_LOCATION_POINT_INTERVAL = 5 * 60 * 1000L //5 минут


private const val WAKE_LOCK_NAME = "OpenTracker:TrackerService"
private const val TAG = "TrackerService"
private const val TRACK_TIMER_CODE = 100


class TrackerService : Service() {

    private val _trackerState = MutableStateFlow(TrackerState())
    val trackerState: StateFlow<TrackerState> = _trackerState.asStateFlow()
    private val actions = MutableSharedFlow<String>(replay = 1)

    private val serviceScope = MainScope()
    private val serviceManager = TrackerManager(TimeManager())
    private val timeManager = TimeManager()
    private val startMode: Int = START_NOT_STICKY
    private val binder: IBinder = LocalBinder()
    private val allowRebind = true

    private var locationProvider:ILocation? = null


    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }


    private val lock: WakeLock by lazy {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME).apply {
            setReferenceCounted(true)
        }
    }


    override fun onCreate() {
        super.onCreate()
        val fusedLocationManager = LocationServices.getFusedLocationProviderClient(this)
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        locationProvider = LocationManager(fusedLocationManager,telephonyManager,::positionsReady)

        Log.d(TAG, "onCreate")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand:$intent")
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
        serviceManager.stopService()
        locationProvider?.stop()

        while (lock.isHeld) {
            lock.release()
        }

        Log.d(TAG, "onDestroy:")
    }



    private fun handleAction(action: String) {

        when (action) {

            Intent.ACTION_BOOT_COMPLETED -> {
                val sTime = restartTimer()
                updateStartTime(sTime)

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
            }

            Intent.ACTION_TIME_CHANGED -> {
                val sTime = restartTimer()
                updateStartTime(sTime)
                if (!timeManager.isInWrkTimeNow()) {
                    stopForeground()
                    updateForegroundState(false)
                }
            }

            TRACKER_CLIENT_BIND -> {
                val currentState = _trackerState.value
                if (currentState.serviceLastStartTime == null) {
                    val sTime = restartTimer()
                    updateStartTime(sTime)
                }
            }

            TRACKER_TIMER_ACTION -> {


                if (timeManager.isInWrkTimeNow()) {

                    val currentState = _trackerState.value
                    if (!currentState.isForeground) {
                        startForeground()
                        updateForegroundState(true)
                    }
                    lock.acquire(2*60*1000)
                    locationProvider?.start()


                } else {

                    val sTime = restartTimer()
                    updateStartTime(sTime)
                    stopForeground()
                    updateForegroundState(false)
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
        val newState = currentState.copy(isForeground = true)
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
        val cur = _trackerState.value

        if (pos.isNotEmpty()){
            val lastPos = pos.last() as PositionGpsData
            val newState = cur.copy(locCount = pos.size, gpsLastTime = lastPos.eventDate)
            _trackerState.update { newState }
        }
        lock.release()
    }

    inner class LocalBinder : Binder() {
        fun getService(): TrackerService = this@TrackerService
    }

}
