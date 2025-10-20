package com.atelierversace.data.repository

import com.atelierversace.data.local.PerfumeDao
import com.atelierversace.data.model.Perfume
import kotlinx.coroutines.flow.Flow

class PerfumeRepository(private val perfumeDao: PerfumeDao) {

    fun getWardrobe(): Flow<List<Perfume>> = perfumeDao.getWardrobe()

    fun getWishlist(): Flow<List<Perfume>> = perfumeDao.getWishlist()

    suspend fun getPerfumeById(id: Int): Perfume? = perfumeDao.getPerfumeById(id)

    suspend fun addPerfume(perfume: Perfume) = perfumeDao.insert(perfume)

    suspend fun deletePerfume(perfume: Perfume) = perfumeDao.delete(perfume)
}