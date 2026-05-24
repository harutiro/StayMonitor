package net.harutiro.nationalweather.features.stayLogDB.repositories

import kotlinx.coroutines.flow.Flow
import net.harutiro.nationalweather.features.stayLogDB.entities.GeofencePinEntity
import net.harutiro.nationalweather.features.stayLogDB.entities.StayLogEntity

class StayLogRepositoryImpl : StayLogRepository {
    private val dao = net.harutiro.nationalweather.Application.database.stayLogDao()

    override fun observeAll(): Flow<List<StayLogEntity>> = dao.observeAll()

    override suspend fun recordEnter(pin: GeofencePinEntity, at: Long) {
        val log =
            StayLogEntity(
                pinId = pin.id,
                pinName = pin.name,
                latitude = pin.latitude,
                longitude = pin.longitude,
                enteredAt = at,
                exitedAt = null,
            )
        dao.insert(log)
    }

    override suspend fun recordExit(pinId: String, at: Long) {
        val active = dao.findActive(pinId) ?: return
        dao.update(active.copy(exitedAt = at))
    }
}
