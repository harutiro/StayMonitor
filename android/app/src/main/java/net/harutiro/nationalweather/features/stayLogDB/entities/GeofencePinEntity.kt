package net.harutiro.nationalweather.features.stayLogDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geofence_pin")
data class GeofencePinEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val createdAt: Long,
)
