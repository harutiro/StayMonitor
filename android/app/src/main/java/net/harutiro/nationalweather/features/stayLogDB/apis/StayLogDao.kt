package net.harutiro.nationalweather.features.stayLogDB.apis

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.harutiro.nationalweather.features.stayLogDB.entities.StayLogEntity

@Dao
interface StayLogDao {
    @Insert
    suspend fun insert(log: StayLogEntity): Long

    @Update
    suspend fun update(log: StayLogEntity)

    @Query("SELECT * FROM stay_log ORDER BY enteredAt DESC")
    fun observeAll(): Flow<List<StayLogEntity>>

    @Query("SELECT * FROM stay_log WHERE pinId = :pinId AND exitedAt IS NULL ORDER BY enteredAt DESC LIMIT 1")
    suspend fun findActive(pinId: String): StayLogEntity?
}
