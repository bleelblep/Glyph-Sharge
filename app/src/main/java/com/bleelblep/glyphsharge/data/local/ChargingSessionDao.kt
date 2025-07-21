package com.bleelblep.glyphsharge.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bleelblep.glyphsharge.data.model.ChargingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingSessionDao {
    @Insert
    suspend fun insert(session: ChargingSession): Long

    @Update
    suspend fun update(session: ChargingSession)

    @Query("SELECT * FROM charging_sessions ORDER BY startTimestamp DESC")
    fun getAllSessions(): Flow<List<ChargingSession>>

    @Query("SELECT * FROM charging_sessions WHERE endTimestamp = 0 LIMIT 1")
    suspend fun getOpenSession(): ChargingSession?

    @Query("SELECT * FROM charging_sessions WHERE startTimestamp >= :since ORDER BY startTimestamp DESC")
    fun getSessionsSince(since: Long): Flow<List<ChargingSession>>

    @Query("DELETE FROM charging_sessions")
    suspend fun clearAll()

    @Delete
    suspend fun deleteSession(session: ChargingSession)
} 