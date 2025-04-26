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
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random
import kotlin.random.nextInt


private const val WAKE_LOCK_NAME = "OpenTracker:TrackerService"
private const val TAG = "TrackerService"
private const val TRACK_TIMER = 100
private const val TRACK_TIMER_ACTION = "intent.action.TIMER_FIRED"
private const val FIRST_START_TIME_INTERVAL = 60 * 1000 //ms

private const val START_WORK_HOUR = 8
private const val END_WORK_HOUR = 20
private const val IS_WORK_TIME_ONLY = true


class TrackerService : Service() {

    private val serviceScope = MainScope()
    private val startMode: Int = START_NOT_STICKY
    private val binder: IBinder = LocalBinder()
    private val allowRebind = true



    private var isForegroundMode = false
    private var mainJob:Job? = null

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val nextPointAlarmManager: AlarmManager by lazy {
        getSystemService(ALARM_SERVICE) as AlarmManager
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
                    dashBoard?.invoke(t)
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
        dashBoard = null
        return allowRebind
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)

        Log.d(TAG,"onRebind...")
    }

    override fun onDestroy() {
        super.onDestroy()
        dashBoard = null
        serviceScope.cancel("Service is destroying...")
        lock.release()
        Log.d(TAG,"onDestroy...")
    }

    fun setUpClient(client:(Int)->Unit){
        dashBoard = client
        Log.d(TAG,"setUpClient...")
    }


    private fun restartTimer(futureTriggerTime:Long, interval:Long){
        val intent  = Intent(this, TrackerReceiver::class.java).apply {
            action = TRACK_TIMER_ACTION
        }

        val tTime = SystemClock.elapsedRealtime() + (futureTriggerTime - System.currentTimeMillis()  )

        val pIntent = PendingIntent.getBroadcast(this, TRACK_TIMER, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        (getSystemService(ALARM_SERVICE) as AlarmManager).apply {
            setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, tTime, interval, pIntent)
        }
    }

    private fun getFistStartTimePoint():Long{

        return System.currentTimeMillis() + FIRST_START_TIME_INTERVAL


    }

    private fun getNextTimePoint ():Long{

        return 100L


    }


    private fun isInWrkTimeNow(): Boolean {
        if (!IS_WORK_TIME_ONLY) return true
        val calendar = Calendar.getInstance()
        return isWrkDay(calendar) && isWrkTime(calendar)
    }


    private fun isWrkTime(calendar: Calendar): Boolean {
        return calendar[Calendar.HOUR_OF_DAY] in START_WORK_HOUR until END_WORK_HOUR

    }


    private fun isWrkDay(cal: Calendar): Boolean {
        val dayOfWeek = cal[Calendar.DAY_OF_WEEK]
        val month = cal[Calendar.MONTH]
        val day = cal[Calendar.DAY_OF_MONTH]
        val year = cal[Calendar.YEAR]

        var isWrkDay = !(dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY)


        // исключения
        if (isWrkDay) {
            //рабочие дни - праздники
            if ((month == Calendar.JANUARY && year == 2025) && (day <= 3 || day == 6 || day == 7 || day == 8)) isWrkDay = false
            else if ((month == Calendar.MAY && year == 2025 ) && (day == 1 || day == 2 || day == 9 || day == 10)) isWrkDay = false
            else if ((month == Calendar.JUNE && year == 2025 ) && (day == 12 || day == 13)) isWrkDay = false
            else if ((month == Calendar.NOVEMBER && year == 2025) && (day == 3 || day == 4)) isWrkDay = false
            else if ((month == Calendar.DECEMBER && year == 2025) && (day == 31)) isWrkDay = false

        } else {
            // выходные - рабочие
            if ((month == Calendar.NOVEMBER && year == 2025) && (day == 1)) isWrkDay = true
        }

        return isWrkDay
    }



    inner class LocalBinder : Binder() {
        fun getService(): TrackerService = this@TrackerService
    }
}
