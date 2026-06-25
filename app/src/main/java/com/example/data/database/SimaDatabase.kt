package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ScanHistoryEntity::class, SocialAccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SimaDatabase : RoomDatabase() {
    abstract fun simaDao(): SimaDao

    companion object {
        @Volatile
        private var INSTANCE: SimaDatabase? = null

        fun getDatabase(context: Context): SimaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SimaDatabase::class.java,
                    "sima_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
