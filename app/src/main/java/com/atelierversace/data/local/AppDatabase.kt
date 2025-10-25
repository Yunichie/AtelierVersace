package com.atelierversace.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.atelierversace.data.model.Perfume

@Database(entities = [Perfume::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfumeDao(): PerfumeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2 (adds topNotes, middleNotes, baseNotes)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE perfumes ADD COLUMN topNotes TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE perfumes ADD COLUMN middleNotes TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE perfumes ADD COLUMN baseNotes TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "atelier_versace_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    // For development only - use fallbackToDestructiveMigration() to recreate DB
                    // Remove this in production
                    // .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
