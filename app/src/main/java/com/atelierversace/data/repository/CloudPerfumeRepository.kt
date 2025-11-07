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
            println("DEBUG - Getting wardrobe for userId: $userId")
            val perfumes = client.from("perfumes")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_wishlist", false)
                    }
                }
                .decodeList<PerfumeCloud>()
                .sortedByDescending { it.timestamp }
            println("DEBUG - Retrieved ${perfumes.size} wardrobe items")
            Result.success(perfumes)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to get wardrobe: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getWishlist(userId: String): Result<List<PerfumeCloud>> {
        return try {
            println("DEBUG - Getting wishlist for userId: $userId")
            val perfumes = client.from("perfumes")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_wishlist", true)
                    }
                }
                .decodeList<PerfumeCloud>()
                .sortedByDescending { it.timestamp }
            println("DEBUG - Retrieved ${perfumes.size} wishlist items")
            Result.success(perfumes)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to get wishlist: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getFavorites(userId: String): Result<List<PerfumeCloud>> {
        return try {
            println("DEBUG - Getting favorites for userId: $userId")
            val perfumes = client.from("perfumes")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_favorite", true)
                    }
                }
                .decodeList<PerfumeCloud>()
                .sortedByDescending { it.timestamp }
            println("DEBUG - Retrieved ${perfumes.size} favorite items")
            Result.success(perfumes)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to get favorites: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addPerfume(perfume: PerfumeCloud): Result<Unit> {
        return try {
            println("DEBUG - Adding perfume: ${perfume.brand} ${perfume.name}")
            println("DEBUG - User ID: ${perfume.userId}")
            println("DEBUG - Is Wishlist: ${perfume.isWishlist}")

            client.from("perfumes").insert(perfume)

            println("DEBUG - Successfully added perfume")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to add perfume: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updatePerfume(perfume: PerfumeCloud): Result<Unit> {
        return try {
            println("DEBUG - Updating perfume: ${perfume.id}")

            if (perfume.id.isNullOrEmpty()) {
                println("ERROR - Cannot update perfume with null/empty ID")
                return Result.failure(Exception("Perfume ID is required for update"))
            }

            client.from("perfumes")
                .update(perfume) {
                    filter {
                        eq("id", perfume.id)
                    }
                }

            println("DEBUG - Successfully updated perfume")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to update perfume: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deletePerfume(perfumeId: String): Result<Unit> {
        return try {
            println("DEBUG - Deleting perfume: $perfumeId")

            if (perfumeId.isEmpty()) {
                println("ERROR - Cannot delete perfume with empty ID")
                return Result.failure(Exception("Perfume ID is required for deletion"))
            }

            client.from("perfumes")
                .delete {
                    filter {
                        eq("id", perfumeId)
                    }
                }

            println("DEBUG - Successfully deleted perfume")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to delete perfume: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(perfumeId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            println("DEBUG - Toggling favorite for: $perfumeId to $isFavorite")

            if (perfumeId.isEmpty()) {
                println("ERROR - Cannot toggle favorite with empty ID")
                return Result.failure(Exception("Perfume ID is required"))
            }

            client.from("perfumes")
                .update(mapOf("is_favorite" to isFavorite)) {
                    filter {
                        eq("id", perfumeId)
                    }
                }

            println("DEBUG - Successfully toggled favorite")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to toggle favorite: ${e.message}")
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