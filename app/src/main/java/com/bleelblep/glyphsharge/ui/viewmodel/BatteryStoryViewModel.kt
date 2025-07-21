package com.bleelblep.glyphsharge.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bleelblep.glyphsharge.data.model.ChargingSession
import com.bleelblep.glyphsharge.data.repository.ChargingSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatteryStoryViewModel @Inject constructor(
    private val repository: ChargingSessionRepository
) : ViewModel() {

    val sessions: StateFlow<List<ChargingSession>> = repository.sessions

    suspend fun clearSessions() {
        try {
            repository.clearAllSessions()
        } catch (e: Exception) {
            Log.e("BatteryStoryViewModel", "Error clearing sessions: ${e.message}")
        }
    }

    suspend fun deleteSession(session: ChargingSession) {
        try {
            repository.deleteSession(session)
        } catch (e: Exception) {
            Log.e("BatteryStoryViewModel", "Error deleting session: ${e.message}")
        }
    }
} 