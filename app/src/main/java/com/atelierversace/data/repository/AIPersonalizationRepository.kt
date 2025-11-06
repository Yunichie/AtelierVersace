package com.atelierversace.data.repository

import com.atelierversace.data.remote.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AIPersonalizationRepository {
    private val client = SupabaseClient.client

    suspend fun getPersonalization(userId: String): Result<AIPersonalization?> {
        return try {
            val personalization = client.from("ai_personalization")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<AIPersonalization>()
            Result.success(personalization)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePersonalization(personalization: AIPersonalization): Result<Unit> {
        return try {
            val existing = getPersonalization(personalization.userId)
            if (existing.getOrNull() != null) {
                client.from("ai_personalization")
                    .update(personalization) {
                        filter {
                            eq("user_id", personalization.userId)
                        }
                    }
            } else {
                client.from("ai_personalization").insert(personalization)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveLayeringRecommendation(recommendation: LayeringRecommendation): Result<Unit> {
        return try {
            client.from("layering_recommendations").insert(recommendation)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLayeringHistory(userId: String): Result<List<LayeringRecommendation>> {
        return try {
            val history = client.from("layering_recommendations")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<LayeringRecommendation>()
                .sortedByDescending { it.createdAt }
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveInteraction(interaction: AIInteraction): Result<Unit> {
        return try {
            client.from("ai_interactions").insert(interaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInteractionHistory(userId: String): Result<List<AIInteraction>> {
        return try {
            val history = client.from("ai_interactions")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<AIInteraction>()
                .sortedByDescending { it.timestamp }
                .take(50)
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeUserPreferences(
        userId: String,
        perfumes: List<PerfumeCloud>
    ): AIPersonalization {
        val brands = perfumes.map { it.brand }.distinct()
        val brandFrequency = brands.groupingBy { it }.eachCount()
        val preferredBrands = brandFrequency.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }

        val allNotes = perfumes.flatMap { perfume ->
            listOf(
                perfume.topNotes.split(","),
                perfume.middleNotes.split(","),
                perfume.baseNotes.split(",")
            ).flatten().map { it.trim() }.filter { it.isNotEmpty() }
        }
        val noteFrequency = allNotes.groupingBy { it }.eachCount()
        val preferredNotes = noteFrequency.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }

        val styleKeywords = mapOf(
            "Fresh" to listOf("citrus", "bergamot", "lemon", "orange", "mint", "aquatic"),
            "Floral" to listOf("rose", "jasmine", "lily", "violet", "peony", "iris"),
            "Woody" to listOf("sandalwood", "cedar", "vetiver", "patchouli", "oak"),
            "Oriental" to listOf("vanilla", "amber", "musk", "incense", "spice"),
            "Fruity" to listOf("apple", "peach", "berry", "pear", "plum")
        )

        val styleScores = styleKeywords.mapValues { (_, keywords) ->
            allNotes.count { note ->
                keywords.any { keyword -> note.contains(keyword, ignoreCase = true) }
            }
        }
        val styleProfile = styleScores.maxByOrNull { it.value }?.key ?: "Balanced"

        return AIPersonalization(
            userId = userId,
            preferredBrands = preferredBrands,
            preferredNotes = preferredNotes,
            commonOccasions = emptyMap(),
            styleProfile = styleProfile,
            intensityPreference = "Moderate",
            lastUpdated = System.currentTimeMillis().toString()
        )
    }

    fun observePersonalization(userId: String): Flow<AIPersonalization?> = flow {
        while (true) {
            val result = getPersonalization(userId)
            if (result.isSuccess) {
                emit(result.getOrNull())
            }
            kotlinx.coroutines.delay(10000)
        }
    }
}