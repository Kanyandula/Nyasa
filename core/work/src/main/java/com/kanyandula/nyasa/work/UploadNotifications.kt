package com.kanyandula.nyasa.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.kanyandula.nyasa.core.work.R

object UploadNotifications {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(UploadKeys.NOTIFICATION_CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            UploadKeys.NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.upload_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.upload_notification_channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun ongoingNotification(context: Context, progressPercent: Int): Notification {
        val pct = progressPercent.coerceIn(0, 100)
        val indeterminate = pct == 0 || pct >= 100
        val text = if (indeterminate) {
            context.getString(R.string.upload_notification_indeterminate)
        } else {
            context.getString(R.string.upload_notification_progress_percent, pct)
        }
        return NotificationCompat.Builder(context, UploadKeys.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.upload_notification_title))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .setProgress(100, pct, indeterminate)
            .setOnlyAlertOnce(true)
            .build()
    }
}