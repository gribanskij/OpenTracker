package com.gribansky.opentracker.core

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import kotlin.random.Random
import kotlin.random.nextInt


class TrackerService : Service() {

    private val tag = TrackerService::class.java.simpleName
    private val handler = Handler(Looper.getMainLooper())

    private val startMode: Int = START_NOT_STICKY
    private val binder: IBinder = LocalBinder()
    private var allowRebind: Boolean = true
    private var dashBoard: ((Int) -> Unit)? = null


    override fun onCreate() {
        super.onCreate()
        handler.postDelayed({
            dashBoard?.invoke(Random.nextInt(0..1000))
        },1000)
        Log.d(tag,"onCreate...")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag,"onStartCommand...")
        return startMode
    }


    override fun onBind(intent: Intent?): IBinder {
        Log.d(tag,"onBind...")
        return binder

    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(tag,"onUnbind...")
        dashBoard = null
        return allowRebind
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)

        Log.d(tag,"onRebind...")
    }

    override fun onDestroy() {
        super.onDestroy()
        dashBoard = null
        Log.d(tag,"onDestroy...")
    }

    fun setUpClient(client:(Int)->Unit){
        dashBoard = client
        Log.d(tag,"setUpClient...")
    }



    inner class LocalBinder : Binder() {
        fun getService(): TrackerService = this@TrackerService
    }

}