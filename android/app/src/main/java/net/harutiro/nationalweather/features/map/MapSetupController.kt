package net.harutiro.nationalweather.features.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapSetupController(
    private val context: Context,
    private val mapView: MapView,
) {
    private val fused = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun setupMapWithLocation(fallback: GeoPoint = GeoPoint(35.681236, 139.767125)) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(17.0)
        mapView.controller.setCenter(fallback)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                mapView.controller.setCenter(GeoPoint(loc.latitude, loc.longitude))
            } else {
                Log.d(TAG, "lastLocation null, using fallback")
            }
        }
        runCatching {
            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
            locationOverlay.enableMyLocation()
            mapView.overlays.add(locationOverlay)
            val compass = CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
            compass.enableCompass()
            mapView.overlays.add(compass)
        }
    }

    companion object {
        private const val TAG = "MapSetupController"
    }
}
