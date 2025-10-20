package com.atelierversace.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.atelierversace.data.model.Perfume

@Database(entities = [Perfume::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfumeDao(): PerfumeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "atelier_versace_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}