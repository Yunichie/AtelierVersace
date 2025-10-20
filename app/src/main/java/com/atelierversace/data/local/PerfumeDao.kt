package com.atelierversace.data.local

import androidx.room.*
import com.atelierversace.data.model.Perfume
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfumeDao {
    @Query("SELECT * FROM perfumes WHERE isWishlist = 0 ORDER BY timestamp DESC")
    fun getWardrobe(): Flow<List<Perfume>>

    @Query("SELECT * FROM perfumes WHERE isWishlist = 1 ORDER BY timestamp DESC")
    fun getWishlist(): Flow<List<Perfume>>

    @Query("SELECT * FROM perfumes WHERE id = :id")
    suspend fun getPerfumeById(id: Int): Perfume?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(perfume: Perfume)

    @Delete
    suspend fun delete(perfume: Perfume)

    @Query("DELETE FROM perfumes")
    suspend fun deleteAll()
}