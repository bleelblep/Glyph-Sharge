package com.bleelblep.glyphsharge.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room schema migrations for GlyphSharge database.
 */
object Migrations {
    /**
     * v1 → v2 : add [sampleCount] column to charging_sessions (default = 1).
     */
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE charging_sessions ADD COLUMN sampleCount INTEGER NOT NULL DEFAULT 1")
        }
    }
} 