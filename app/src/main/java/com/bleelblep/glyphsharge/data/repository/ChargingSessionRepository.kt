package com.bleelblep.glyphsharge.data.repository

import android.util.Log
import com.bleelblep.glyphsharge.data.local.ChargingSessionDao
import com.bleelblep.glyphsharge.data.model.ChargingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChargingSessionRepository @Inject constructor(
    private val dao: ChargingSessionDao
) {
    private val TAG = "ChargeRepo"
    private val _sessions = MutableStateFlow<List<ChargingSession>>(emptyList())
    val sessions: StateFlow<List<ChargingSession>> = _sessions.asStateFlow()

    init {
        // Initialize sessions list
        _sessions.value = emptyList()
    }

    fun observeAll(): Flow<List<ChargingSession>> = dao.getAllSessions()

    suspend fun startSession(initialPercentage: Int, temperatureC: Float) {
        val now = System.currentTimeMillis()
        val session = ChargingSession(
            startTimestamp = now,
            endTimestamp = 0,
            startPercentage = initialPercentage,
            endPercentage = initialPercentage,
            avgTemperatureC = temperatureC,
            sampleCount = 1
        )
        dao.insert(session)
        _sessions.value = dao.getAllSessions().first()
    }

    suspend fun finishSession(endPercentage: Int, temperatureC: Float) {
        val openSession = dao.getOpenSession()
        if (openSession == null) {
            Log.w(TAG, "No open session found to finish")
            return
        }

        val now = System.currentTimeMillis()
        val duration = now - openSession.startTimestamp
        val newAvgTemp = (openSession.avgTemperatureC * openSession.sampleCount + temperatureC) / (openSession.sampleCount + 1)

        try {
            val updatedSession = openSession.copy(
                endTimestamp = now,
                endPercentage = endPercentage,
                avgTemperatureC = newAvgTemp,
                sampleCount = openSession.sampleCount + 1
            )
            
            dao.update(updatedSession)
            Log.d(TAG, "Session finished: duration=${duration}ms, finalPct=$endPercentage, avgTemp=$newAvgTemp")
            
            // Force refresh the sessions list
            _sessions.value = dao.getAllSessions().first()
        } catch (e: Exception) {
            Log.e(TAG, "Error finishing session", e)
        }
    }

    suspend fun updateOngoingSession(percentage: Int, temperature: Float) {
        val openSession = dao.getOpenSession()
        if (openSession == null) {
            Log.w(TAG, "No open session found to update")
            return
        }

        val now = System.currentTimeMillis()
        val newAvgTemp = (openSession.avgTemperatureC * openSession.sampleCount + temperature) / (openSession.sampleCount + 1)

        try {
            val updatedSession = openSession.copy(
                endPercentage = percentage,
                avgTemperatureC = newAvgTemp,
                sampleCount = openSession.sampleCount + 1
            )
            
            dao.update(updatedSession)
            
            // Force refresh the sessions list
            _sessions.value = dao.getAllSessions().first()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating session", e)
        }
    }

    suspend fun clearAllSessions() {
        try {
            dao.clearAll()
            _sessions.value = emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing sessions", e)
        }
    }

    suspend fun deleteSession(session: ChargingSession) {
        try {
            dao.deleteSession(session)
            _sessions.value = dao.getAllSessions().first()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting session", e)
        }
    }

    suspend fun getOpenSession(): ChargingSession? = dao.getOpenSession()
} 