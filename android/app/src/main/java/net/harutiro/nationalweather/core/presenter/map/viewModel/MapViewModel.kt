package net.harutiro.nationalweather.core.presenter.map.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.harutiro.nationalweather.features.geofence.GeofenceRepository
import net.harutiro.nationalweather.features.stayLogDB.entities.GeofencePinEntity
import net.harutiro.nationalweather.features.stayLogDB.repositories.GeofencePinRepository
import net.harutiro.nationalweather.features.stayLogDB.repositories.GeofencePinRepositoryImpl
import java.util.UUID

class MapViewModel(app: Application) : AndroidViewModel(app) {
    private val pinRepo: GeofencePinRepository = GeofencePinRepositoryImpl()
    private val geofenceRepo: GeofenceRepository = GeofenceRepository(app.applicationContext)

    private val _pendingLatLng = MutableStateFlow<Pair<Double, Double>?>(null)
    val pendingLatLng: StateFlow<Pair<Double, Double>?> = _pendingLatLng.asStateFlow()

    val pins: StateFlow<List<GeofencePinEntity>> = pinRepo.observeAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList(),
    )

    private val _selectedPinId = MutableStateFlow<String?>(null)
    val selectedPin: StateFlow<GeofencePinEntity?> = combine(pins, _selectedPinId) { list, id ->
        if (id == null) null else list.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setPendingPin(latitude: Double, longitude: Double) {
        _pendingLatLng.value = latitude to longitude
    }

    fun clearPending() {
        _pendingLatLng.value = null
    }

    fun registerPin(name: String) {
        val pending = _pendingLatLng.value ?: return
        val pin = GeofencePinEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            latitude = pending.first,
            longitude = pending.second,
            radiusMeters = 100f,
            createdAt = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            pinRepo.upsert(pin)
            geofenceRepo.addGeofence(pin) { /* エラーは握りつぶす */ }
            _pendingLatLng.value = null
        }
    }

    fun removePin(pinId: String) {
        viewModelScope.launch {
            geofenceRepo.removeGeofence(pinId)
            pinRepo.deleteById(pinId)
            if (_selectedPinId.value == pinId) {
                _selectedPinId.value = null
            }
        }
    }

    fun selectPin(pinId: String?) {
        _selectedPinId.value = pinId
    }

    fun renamePin(pinId: String, newName: String) {
        viewModelScope.launch {
            val pin = pinRepo.findById(pinId) ?: return@launch
            pinRepo.upsert(pin.copy(name = newName))
            selectPin(null)
        }
    }

    fun movePin(pinId: String, newLatitude: Double, newLongitude: Double) {
        viewModelScope.launch {
            val pin = pinRepo.findById(pinId) ?: return@launch
            val updated = pin.copy(latitude = newLatitude, longitude = newLongitude)
            pinRepo.upsert(updated)
            geofenceRepo.removeGeofence(pinId)
            geofenceRepo.addGeofence(updated) { /* エラーは握りつぶす */ }
        }
    }

    fun stopAll() {
        viewModelScope.launch {
            geofenceRepo.removeAll()
        }
    }
}
