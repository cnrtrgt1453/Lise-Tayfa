package com.cotx.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cotx.app.MainActivity
import com.cotx.app.R

object NotificationHelper {

    const val CHANNEL_ID = "cotx_notifications_channel"
    const val CHANNEL_NAME = "cotx Bildirimleri"
    const val CHANNEL_DESC = "Beğeniler, çözümler, yorumlar ve takipçi bildirimleri"

    const val EXTRA_QUESTION_ID = "extra_question_id"
    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"

    /**
     * Creates notification channel for Android 8.0 (API 26) and above.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a system notification in the status bar with sound & vibration.
     */
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        questionId: String? = null,
        notificationType: String? = null,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        try {
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            if (!notificationManagerCompat.areNotificationsEnabled()) {
                return
            }

            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (!questionId.isNullOrEmpty()) {
                    putExtra(EXTRA_QUESTION_ID, questionId)
                }
                if (!notificationType.isNullOrEmpty()) {
                    putExtra(EXTRA_NOTIFICATION_TYPE, notificationType)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

            notificationManagerCompat.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
