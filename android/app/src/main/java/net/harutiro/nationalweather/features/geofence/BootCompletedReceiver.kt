package net.harutiro.nationalweather.features.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.harutiro.nationalweather.features.stayLogDB.repositories.GeofencePinRepositoryImpl

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        Log.d(TAG, "boot completed, re-registering geofences")
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pins = GeofencePinRepositoryImpl().observeAll().first()
                if (pins.isEmpty()) {
                    Log.d(TAG, "no pins to re-register")
                    return@launch
                }
                GeofenceRepository(appContext).removeAll()
                GeofenceRepository(appContext).addGeofences(pins) {
                    Log.e(TAG, "re-register failed", it)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "boot handler crashed", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
