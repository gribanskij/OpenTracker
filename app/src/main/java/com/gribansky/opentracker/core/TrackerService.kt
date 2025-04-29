package com.gribansky.opentracker.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.SystemClock
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.random.nextInt


private const val WAKE_LOCK_NAME = "OpenTracker:TrackerService"
private const val TAG = "TrackerService"
private const val TRACK_TIMER = 100
private const val TRACK_TIMER_ACTION = "intent.action.TIMER_FIRED"



class TrackerService : Service() {

    private val _trackerState = MutableStateFlow(0)
    val trackerState:StateFlow<Int> = _trackerState

    private val serviceScope = MainScope()
    private val startMode: Int = START_NOT_STICKY
    private val binder: IBinder = LocalBinder()
    private val allowRebind = true


    private var isForegroundMode = false
    private var mainJob:Job? = null

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }


    private val lock: WakeLock by lazy {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME).apply {
            setReferenceCounted(false)
        }
    }

    private var dashBoard: ((Int) -> Unit)? = null


    override fun onCreate() {
        super.onCreate()
        lock.acquire()
        Log.d(TAG,"onCreate...")



    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"onStartCommand...mode:${flags} id: ${startId}")



        if (mainJob?.isActive != true){

            mainJob = serviceScope.launch {
                repeat(1000){
                    val t = Random.nextInt(1..100)
                    _trackerState.emit(t)
                    delay(1000)
                }
            }
        }

        return startMode
    }


    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG,"onBind...")
        return binder

    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG,"onUnbind...")
        return allowRebind
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)

        Log.d(TAG,"onRebind...")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel("Service is destroying...")
        lock.release()
        Log.d(TAG,"onDestroy...")
    }


    private fun restartTimer(futureTriggerTime:Long, interval:Long){
        val intent  = Intent(this, TrackerReceiver::class.java).apply {
            action = TRACK_TIMER_ACTION
        }

        val tTime = SystemClock.elapsedRealtime() + futureTriggerTime - System.currentTimeMillis()

        val pIntent = PendingIntent.getBroadcast(this, TRACK_TIMER, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        (getSystemService(ALARM_SERVICE) as AlarmManager).apply {
            setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, tTime, interval, pIntent)
        }
    }




    inner class LocalBinder : Binder() {
        fun getService(): TrackerService = this@TrackerService
    }
}
