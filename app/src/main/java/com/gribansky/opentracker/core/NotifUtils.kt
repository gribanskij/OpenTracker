package com.gribansky.opentracker.core

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gribansky.opentracker.R
import com.gribansky.opentracker.TrackerActivity

object NotificationConstants {
    const val CHANNEL_ID = "tracker_channel"
    const val FOREGROUND_NOTIFICATION_ID = 1001
    const val UPDATE_NOTIFICATION_ID = 1002
    const val ERROR_NOTIFICATION_ID = 1003
}

object NotificationUtils {

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationConstants.CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }

            ContextCompat.getSystemService(context, NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    private fun createBaseBuilder(
        context: Context,
        @StringRes titleRes: Int,
        @StringRes textRes: Int,
        iconRes: Int = android.R.drawable.ic_dialog_info
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(context.getString(titleRes))
            .setContentText(context.getString(textRes))
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
    }

    fun getForegroundNotification(context: Context): Notification {
        val intent = Intent(context, TrackerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return createBaseBuilder(
            context,
            R.string.tracker_title,
            R.string.tracker_running_text
        ).apply {
            setOngoing(true)
            setAutoCancel(false)
            setContentIntent(pendingIntent)
            setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            setSmallIcon(android.R.drawable.ic_dialog_map)
        }.build()
    }

    fun showFakeWarning(context: Context) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        createBaseBuilder(
            context,
            R.string.warning_title,
            R.string.fake_location_warning_text,
            android.R.drawable.ic_dialog_alert
        ).apply {
            setSound(soundUri)
            setAutoCancel(true)
        }.notify(context, NotificationConstants.FOREGROUND_NOTIFICATION_ID)
    }

    fun showUpdateNotification(context: Context, info: String) {
        val intent = Intent(context, TrackerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        createBaseBuilder(
            context,
            R.string.update_available_title,
            R.string.update_available_text,
            android.R.drawable.ic_dialog_alert
        ).apply {
            setContentTitle(context.getString(R.string.update_available_title, info))
            setContentIntent(pendingIntent)
        }.notify(context, NotificationConstants.UPDATE_NOTIFICATION_ID)
    }

    fun showErrorNotification(context: Context, title: String?, message: String?) {
        val intent = Intent(context, TrackerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(message)
            .notify(context, NotificationConstants.ERROR_NOTIFICATION_ID)
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }


    private fun NotificationCompat.Builder.notify(
        context: Context,
        notificationId: Int
    ) {
        NotificationManagerCompat.from(context).notify(notificationId, this.build())
    }
}