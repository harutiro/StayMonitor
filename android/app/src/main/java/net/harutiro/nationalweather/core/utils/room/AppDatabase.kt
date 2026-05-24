package net.harutiro.nationalweather.core.utils.room

import androidx.room.Database
import androidx.room.RoomDatabase
import net.harutiro.nationalweather.features.stayLogDB.apis.GeofencePinDao
import net.harutiro.nationalweather.features.stayLogDB.apis.StayLogDao
import net.harutiro.nationalweather.features.stayLogDB.entities.GeofencePinEntity
import net.harutiro.nationalweather.features.stayLogDB.entities.StayLogEntity

@Database(
    entities = [StayLogEntity::class, GeofencePinEntity::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stayLogDao(): StayLogDao

    abstract fun geofencePinDao(): GeofencePinDao
}
