package net.harutiro.nationalweather.features.stayLogDB.repositories

import kotlinx.coroutines.flow.Flow
import net.harutiro.nationalweather.features.stayLogDB.entities.GeofencePinEntity

class GeofencePinRepositoryImpl : GeofencePinRepository {
    private val dao = net.harutiro.nationalweather.Application.database.geofencePinDao()

    override fun observeAll(): Flow<List<GeofencePinEntity>> = dao.observeAll()

    override suspend fun upsert(pin: GeofencePinEntity) = dao.upsert(pin)

    override suspend fun deleteById(id: String) = dao.deleteById(id)

    override suspend fun findById(id: String): GeofencePinEntity? = dao.findById(id)
}
