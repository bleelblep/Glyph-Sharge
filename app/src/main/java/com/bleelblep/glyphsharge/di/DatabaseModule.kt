package com.bleelblep.glyphsharge.di

import android.content.Context
import androidx.room.Room
import com.bleelblep.glyphsharge.data.local.ChargingSessionDao
import com.bleelblep.glyphsharge.data.local.GlyphShargeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GlyphShargeDatabase =
        Room.databaseBuilder(context, GlyphShargeDatabase::class.java, "glyphsharge.db")
            .addMigrations(com.bleelblep.glyphsharge.data.local.Migrations.MIGRATION_1_2)
            .build()

    @Provides
    fun provideChargingSessionDao(db: GlyphShargeDatabase): ChargingSessionDao = db.chargingSessionDao()
} 