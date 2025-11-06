package com.atelierversace.data.repository

import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CloudPerfumeRepository {
    private val client = SupabaseClient.client

    suspend fun getWardrobe(userId: String): Result<List<PerfumeCloud>> {
        return try {
            val perfumes = client.from("perfumes")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_wishlist", false)
                    }
                }
                .decodeList<PerfumeCloud>()
                .sortedByDescending { it.timestamp }
            Result.success(perfumes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWishlist(userId: String): Result<List<PerfumeCloud>> {
        return try {
            val perfumes = client.from("perfumes")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_wishlist", true)
                    }
                }
                .decodeList<PerfumeCloud>()
                .sortedByDescending { it.timestamp }
            Result.success(perfumes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavorites(userId: String): Result<List<PerfumeCloud>> {
        return try {
            val perfumes = client.from("perfumes")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_favorite", true)
                    }
                }
                .decodeList<PerfumeCloud>()
                .sortedByDescending { it.timestamp }
            Result.success(perfumes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPerfume(perfume: PerfumeCloud): Result<Unit> {
        return try {
            client.from("perfumes").insert(perfume)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePerfume(perfume: PerfumeCloud): Result<Unit> {
        return try {
            client.from("perfumes")
                .update(perfume) {
                    filter {
                        eq("id", perfume.id ?: "")
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePerfume(perfumeId: String): Result<Unit> {
        return try {
            client.from("perfumes")
                .delete {
                    filter {
                        eq("id", perfumeId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(perfumeId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            client.from("perfumes")
                .update(mapOf("is_favorite" to isFavorite)) {
                    filter {
                        eq("id", perfumeId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeWardrobe(userId: String): Flow<List<PerfumeCloud>> = flow {
        while (true) {
            val result = getWardrobe(userId)
            if (result.isSuccess) {
                emit(result.getOrNull() ?: emptyList())
            }
            kotlinx.coroutines.delay(5000)
        }
    }

    fun observeWishlist(userId: String): Flow<List<PerfumeCloud>> = flow {
        while (true) {
            val result = getWishlist(userId)
            if (result.isSuccess) {
                emit(result.getOrNull() ?: emptyList())
            }
            kotlinx.coroutines.delay(5000)
        }
    }
}