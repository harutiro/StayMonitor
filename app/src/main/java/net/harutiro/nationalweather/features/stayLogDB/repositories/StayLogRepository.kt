package net.harutiro.nationalweather.features.stayLogDB.repositories

import kotlinx.coroutines.flow.Flow
import net.harutiro.nationalweather.features.stayLogDB.entities.GeofencePinEntity
import net.harutiro.nationalweather.features.stayLogDB.entities.StayLogEntity

interface StayLogRepository {
    fun observeAll(): Flow<List<StayLogEntity>>

    suspend fun recordEnter(pin: GeofencePinEntity, at: Long)

    suspend fun recordExit(pinId: String, at: Long)
}
