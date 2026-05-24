package net.harutiro.nationalweather.features.map

import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay

class MapTapController(
    mapView: MapView,
    private val onTap: (GeoPoint) -> Unit,
) {
    init {
        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null) onTap(p)
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }
        mapView.overlays.add(MapEventsOverlay(receiver))
    }
}
