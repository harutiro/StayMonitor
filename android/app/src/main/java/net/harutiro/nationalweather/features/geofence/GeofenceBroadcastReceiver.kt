package net.harutiro.nationalweather.features.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.harutiro.nationalweather.features.notification.GeoNotification
import net.harutiro.nationalweather.features.stayLogDB.repositories.GeofencePinRepositoryImpl
import net.harutiro.nationalweather.features.stayLogDB.repositories.StayLogRepositoryImpl

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.e(TAG, "geofence error: ${GeofenceStatusCodes.getStatusCodeString(event.errorCode)}")
            return
        }
        val transition = event.geofenceTransition
        val triggers = event.triggeringGeofences ?: return
        val now = System.currentTimeMillis()
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pinRepo = GeofencePinRepositoryImpl()
                val stayRepo = StayLogRepositoryImpl()
                for (g in triggers) {
                    val pin = pinRepo.findById(g.requestId)
                    if (pin == null) {
                        Log.w(TAG, "pin not found for requestId=${g.requestId}")
                        continue
                    }
                    when (transition) {
                        Geofence.GEOFENCE_TRANSITION_ENTER -> {
                            stayRepo.recordEnter(pin, now)
                            GeoNotification.show(context, "「${pin.name}」に入室しました")
                        }
                        Geofence.GEOFENCE_TRANSITION_EXIT -> {
                            stayRepo.recordExit(pin.id, now)
                            GeoNotification.show(context, "「${pin.name}」から退出しました")
                        }
                        else -> {
                            Log.w(TAG, "unknown transition: $transition")
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "failed to handle geofence event", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }
}
