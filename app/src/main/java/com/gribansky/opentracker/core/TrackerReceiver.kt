package com.gribansky.opentracker.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build


class TrackerReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {


        val serviceIntent = Intent(context, TrackerService::class.java)
        intent?.action?.let { serviceIntent.action = it }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(serviceIntent)
            } else {
                context?.startService(serviceIntent)
            }
        } catch (ex:Throwable){
            ex.printStackTrace()
        }
    }
}