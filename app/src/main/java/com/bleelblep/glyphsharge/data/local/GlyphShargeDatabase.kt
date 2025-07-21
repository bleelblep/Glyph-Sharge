package com.bleelblep.glyphsharge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bleelblep.glyphsharge.data.model.ChargingSession

@Database(
    entities = [ChargingSession::class],
    version = 2,
    exportSchema = false
)
abstract class GlyphShargeDatabase : RoomDatabase() {
    abstract fun chargingSessionDao(): ChargingSessionDao
} 