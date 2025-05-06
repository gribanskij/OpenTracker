package com.gribansky.opentracker.core

import android.app.ActionBar
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
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



const val TRACKER_TIMER_ACTION = "intent.action.TIMER_FIRED"
const val TRACKER_CLIENT_BIND = "intent.action.CLIENT_BIND"
const val TRACKER_LOCATION_POINT_INTERVAL = 5*60*1000L //5 минут


private const val WAKE_LOCK_NAME = "OpenTracker:TrackerService"
private const val TAG = "TrackerService"
private const val TRACK_TIMER_CODE = 100




class TrackerService : Service() {

    private val _trackerState = MutableStateFlow(TrackerState())
    val trackerState:StateFlow<TrackerState> = _trackerState.asStateFlow()

    private val serviceScope = MainScope()
    private val serviceManager = TrackerManager(TimeManager())
    private val startMode: Int = START_NOT_STICKY
    private val binder: IBinder = LocalBinder()
    private val allowRebind = true


    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }


    private val lock: WakeLock by lazy {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME).apply {
            setReferenceCounted(false)
        }
    }




    override fun onCreate() {
        super.onCreate()
        //lock.acquire()
        //serviceScope.observeCommands()
        //serviceScope.observeState()

        Log.d(TAG,"onCreate")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"onStartCommand:$intent.action")
        serviceScope.handleAction(intent?.action?:"")
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
        lock.release()

        Log.d(TAG,"onDestroy:")
    }

    private fun CoroutineScope.observeState() = this.launch  {
        serviceManager.trackerState.collect{
            _trackerState.emit(it)
        }
    }

    private fun CoroutineScope.observeCommands() = this.launch {

        serviceManager.commands.collect{com->

            Log.d(TAG,"command:$com")

            when(com){
                is RestartTimer -> {
                    restartTimer(com.startTime,TRACKER_LOCATION_POINT_INTERVAL )
                }
                is StopForeground -> {
                    stopForeground()
                }
                is StartForeground -> {
                    startForeground()
                }
                is StartCollectLocPoints -> {
                    startForeground()
                }

            }
        }
    }

    private fun restartTimer(futureTriggerTime:Long, interval:Long){
        val intent  = Intent(this, TrackerReceiver::class.java).apply {
            action = TRACKER_TIMER_ACTION
        }
        val tTime = SystemClock.elapsedRealtime() + futureTriggerTime - System.currentTimeMillis()
        val pIntent = PendingIntent.getBroadcast(this, TRACK_TIMER_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        (getSystemService(ALARM_SERVICE) as AlarmManager).apply {
            setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, tTime, interval, pIntent)
        }
    }

    private fun startForeground(){

        val notification = getNotification(this)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        ServiceCompat.startForeground(
            this,
            FOREGROUND_NOTIFICATION_ID,
            notification,
            type
        )
    }

    private fun stopForeground(){
        stopForeground(STOP_FOREGROUND_REMOVE)
    }


    private fun CoroutineScope.handleAction(action:String) = this.launch{


        when(action){

            Intent.ACTION_BOOT_COMPLETED -> {
                TrackerAction.PHONE_RESTARTED
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                TrackerAction.APP_UPDATED
            }
            Intent.ACTION_DATE_CHANGED -> {
                TrackerAction.DATE_CHANGED
            }
            Intent.ACTION_TIME_CHANGED -> {
                TrackerAction.TIME_SET
            }
            Intent.ACTION_TIMEZONE_CHANGED -> {
                TrackerAction.TIMEZONE_CHANGED
            }
            TRACKER_CLIENT_BIND -> {
                TrackerAction.CLIENT_BIND
            }
            TRACKER_TIMER_ACTION -> {
                TrackerAction.NEXT_POINT
            }
            else -> {
                TrackerAction.UNDEFINED
            }
        }
    }


    inner class LocalBinder : Binder() {
        fun getService(): TrackerService = this@TrackerService
    }
}
