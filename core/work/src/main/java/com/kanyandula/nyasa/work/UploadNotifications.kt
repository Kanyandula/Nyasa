package com.kanyandula.nyasa.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService

object UploadNotifications {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(UploadKeys.NOTIFICATION_CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            UploadKeys.NOTIFICATION_CHANNEL_ID,
            UploadKeys.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Progress for blog uploads running in the background."
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun ongoingNotification(context: Context, progressPercent: Int): Notification {
        val pct = progressPercent.coerceIn(0, 100)
        val indeterminate = pct == 0 || pct >= 100
        val text = if (indeterminate) "Working…" else "$pct%"
        return NotificationCompat.Builder(context, UploadKeys.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Publishing your post")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .setProgress(100, pct, indeterminate)
            .setOnlyAlertOnce(true)
            .build()
    }
}
