package net.harutiro.nationalweather.features.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import net.harutiro.nationalweather.R
import androidx.core.net.toUri

object GeoNotification {
    private const val CHANNEL_ID = "stay_monitor_geofence"
    private const val CHANNEL_NAME = "滞在通知"

    fun show(context: Context, message: String) {
        ensureChannel(context)

        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://google.com/".toUri()
        )

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(message)
            .setContentText("この通知をタップしたらGoogleに飛びます")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        // POST_NOTIFICATIONS 権限がない端末でも例外を出さないように try-catch
        try {
            NotificationManagerCompat.from(context).notify(message.hashCode(), notif)
        } catch (_: SecurityException) {
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                mgr.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT),
                )
            }
        }
    }
}
