package net.harutiro.nationalweather.features.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import net.harutiro.nationalweather.features.stayLogDB.entities.GeofencePinEntity

class GeofenceRepository(private val context: Context) {
    private val client: GeofencingClient = LocationServices.getGeofencingClient(context)

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    private fun buildGeofence(pin: GeofencePinEntity): Geofence =
        Geofence.Builder()
            .setRequestId(pin.id)
            .setCircularRegion(pin.latitude, pin.longitude, pin.radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setNotificationResponsiveness(5000)
            .build()

    @SuppressLint("MissingPermission")
    fun addGeofence(pin: GeofencePinEntity, onError: (Throwable) -> Unit = {}) {
        if (!hasFineLocationPermission()) {
            onError(SecurityException("ACCESS_FINE_LOCATION not granted"))
            return
        }
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(buildGeofence(pin))
            .build()
        client.addGeofences(request, pendingIntent)
            .addOnSuccessListener { Log.d(TAG, "geofence added: ${pin.id}") }
            .addOnFailureListener {
                Log.e(TAG, "geofence add failed", it)
                onError(it)
            }
    }

    @SuppressLint("MissingPermission")
    fun addGeofences(pins: List<GeofencePinEntity>, onError: (Throwable) -> Unit = {}) {
        if (pins.isEmpty()) return
        if (!hasFineLocationPermission()) {
            onError(SecurityException("ACCESS_FINE_LOCATION not granted"))
            return
        }
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(pins.map(::buildGeofence))
            .build()
        client.addGeofences(request, pendingIntent)
            .addOnSuccessListener { Log.d(TAG, "${pins.size} geofences re-registered") }
            .addOnFailureListener {
                Log.e(TAG, "batch geofence add failed", it)
                onError(it)
            }
    }

    fun removeGeofence(pinId: String) {
        client.removeGeofences(listOf(pinId))
            .addOnSuccessListener { Log.d(TAG, "geofence removed: $pinId") }
            .addOnFailureListener { Log.e(TAG, "geofence remove failed", it) }
    }

    fun removeAll() {
        client.removeGeofences(pendingIntent)
            .addOnSuccessListener { Log.d(TAG, "all geofences removed") }
            .addOnFailureListener { Log.e(TAG, "remove all failed", it) }
    }

    private fun hasFineLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val TAG = "GeofenceRepository"
    }
}
