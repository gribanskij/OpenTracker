package com.gribansky.opentracker.core

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gribansky.opentracker.MainActivity



const val CHANNEL_ID = "1000"
const val notifID = 454
const val notifUpID = 455
const val notifError = 456


fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name: CharSequence = "Open Tracker"
        val description = "Tracker events"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        )
        notificationManager?.createNotificationChannel(channel)
    }
}


fun getNotification(context: Context): Notification {
    val contentIntent = Intent(context, MainActivity::class.java)
    val pendingIntent =
        PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE)
    val builder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setContentTitle("Трекер")
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentText("Трекер запущен")
    return builder.build()
}


@SuppressLint("MissingPermission")
fun showFakeWarning(context: Context) {
    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val builder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setSound(soundUri)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setContentTitle("Предупреждение")
            .setContentText("Обнаружено подложное местоположение. Трекер не работает")
            .setAutoCancel(true)
    NotificationManagerCompat.from(context).notify(notifID, builder.build())

}


@SuppressLint("MissingPermission")
fun showUpdateNeed(context: Context, info: String) {
    val intent = Intent(context, MainActivity::class.java)


    val pendingIntent = PendingIntent.getActivity(context,
        0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val contentTitle: CharSequence = "Доступно обновление $info"
    val contentText: CharSequence = "Нажмите для обновления"

    val builder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setContentIntent(pendingIntent)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
    NotificationManagerCompat.from(context).notify(notifUpID, builder.build())
}


@SuppressLint("MissingPermission")
fun showError(context: Context, title: String?, mes: String?) {
    val intent = Intent(context, MainActivity::class.java)


    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )


    val builder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(mes)
    val manager = NotificationManagerCompat.from(context)
    manager.notify(notifError, builder.build())
}


fun cancelUpdateNotify(context: Context) {
    NotificationManagerCompat.from(context).cancel(notifUpID)
}

fun cancelErrorNotify(context: Context) {
    NotificationManagerCompat.from(context).cancel(notifError)
}


fun hideFakeWarning(context: Context) {
    NotificationManagerCompat.from(context).cancel(notifID)
}
