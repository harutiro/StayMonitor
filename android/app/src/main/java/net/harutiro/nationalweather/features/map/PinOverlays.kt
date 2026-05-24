package net.harutiro.nationalweather.features.map

import net.harutiro.nationalweather.features.stayLogDB.entities.GeofencePinEntity
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

private const val PIN_OVERLAY_TAG = "stay-pin"

fun MapView.renderPins(
    pins: List<GeofencePinEntity>,
    onPinClick: (pinId: String) -> Unit = {},
    onPinDragEnd: (pinId: String, latitude: Double, longitude: Double) -> Unit = { _, _, _ -> },
) {
    overlays.removeAll { it is Marker && it.relatedObject == PIN_OVERLAY_TAG }
    overlays.removeAll { it is Polygon && it.id == PIN_OVERLAY_TAG }
    pins.forEach { pin ->
        val marker = Marker(this).apply {
            id = pin.id
            relatedObject = PIN_OVERLAY_TAG
            position = GeoPoint(pin.latitude, pin.longitude)
            title = pin.name
            isDraggable = true
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { marker, _ ->
                onPinClick(marker.id)
                true
            }
            setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                override fun onMarkerDragStart(m: Marker?) {}
                override fun onMarkerDrag(m: Marker?) {}
                override fun onMarkerDragEnd(m: Marker?) {
                    val finished = m ?: return
                    onPinDragEnd(finished.id ?: return, finished.position.latitude, finished.position.longitude)
                }
            })
        }
        val circle = Polygon().apply {
            id = PIN_OVERLAY_TAG
            points = Polygon.pointsAsCircle(GeoPoint(pin.latitude, pin.longitude), pin.radiusMeters.toDouble())
            fillPaint.color = 0x3300AA00
            outlinePaint.color = 0xFF00AA00.toInt()
            outlinePaint.strokeWidth = 4f
        }
        overlays.add(circle)
        overlays.add(marker)
    }
    invalidate()
}

fun MapView.clearPins() {
    overlays.removeAll { (it is Marker && it.relatedObject == PIN_OVERLAY_TAG) || (it is Polygon && it.id == PIN_OVERLAY_TAG) }
    invalidate()
}
