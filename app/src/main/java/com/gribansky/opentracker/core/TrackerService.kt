package com.gribansky.opentracker.core

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder


class TrackerService : Service() {

    private var startMode: Int = START_NOT_STICKY
    private var binder: IBinder = LocalBinder()
    private var allowRebind: Boolean = false


    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
    }



    inner class LocalBinder : Binder() {
        fun getService(): TrackerService = this@TrackerService
    }

}