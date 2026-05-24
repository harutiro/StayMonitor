package net.harutiro.nationalweather.features.stayLogDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stay_log")
data class StayLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pinId: String,
    val pinName: String,
    val latitude: Double,
    val longitude: Double,
    val enteredAt: Long,
    val exitedAt: Long? = null,
)
