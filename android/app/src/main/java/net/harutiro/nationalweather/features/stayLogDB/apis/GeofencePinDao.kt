package net.harutiro.nationalweather.features.stayLogDB.apis

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.harutiro.nationalweather.features.stayLogDB.entities.GeofencePinEntity

@Dao
interface GeofencePinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pin: GeofencePinEntity)

    @Query("DELETE FROM geofence_pin WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM geofence_pin ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GeofencePinEntity>>

    @Query("SELECT * FROM geofence_pin WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): GeofencePinEntity?
}
