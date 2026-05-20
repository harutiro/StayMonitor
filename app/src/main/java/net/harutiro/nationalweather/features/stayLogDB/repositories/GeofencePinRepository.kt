package net.harutiro.nationalweather.features.stayLogDB.repositories

import kotlinx.coroutines.flow.Flow
import net.harutiro.nationalweather.features.stayLogDB.entities.GeofencePinEntity

interface GeofencePinRepository {
    fun observeAll(): Flow<List<GeofencePinEntity>>

    suspend fun upsert(pin: GeofencePinEntity)

    suspend fun deleteById(id: String)

    suspend fun findById(id: String): GeofencePinEntity?
}
