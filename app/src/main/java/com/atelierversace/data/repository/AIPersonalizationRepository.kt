package com.atelierversace.data.repository

import com.atelierversace.data.remote.*
import com.atelierversace.utils.PersonalizedGeminiHelper
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AIPersonalizationRepository {
    private val client = SupabaseClient.client
    private val geminiHelper = PersonalizedGeminiHelper()

    suspend fun getPersonalization(userId: String): Result<AIPersonalization?> {
        return try {
            println("DEBUG - Getting personalization for userId: $userId")
            val personalization = client.from("ai_personalization")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<AIPersonalization>()

            if (personalization != null) {
                println("DEBUG - Personalization found: ${personalization.styleProfile}")
            } else {
                println("DEBUG - No personalization found")
            }

            Result.success(personalization)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to get personalization: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updatePersonalization(personalization: AIPersonalization): Result<Unit> {
        return try {
            println("DEBUG - Updating personalization for userId: ${personalization.userId}")

            val existing = getPersonalization(personalization.userId)
            if (existing.getOrNull() != null) {
                println("DEBUG - Personalization exists, updating")
                client.from("ai_personalization")
                    .update(personalization) {
                        filter {
                            eq("user_id", personalization.userId)
                        }
                    }
            } else {
                println("DEBUG - Personalization doesn't exist, inserting")
                client.from("ai_personalization").insert(personalization)
            }

            println("DEBUG - Personalization saved successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to update personalization: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun saveLayeringRecommendation(recommendation: LayeringRecommendation): Result<Unit> {
        return try {
            println("DEBUG - Saving layering recommendation for userId: ${recommendation.userId}")
            client.from("layering_recommendations").insert(recommendation)
            println("DEBUG - Layering recommendation saved")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to save layering recommendation: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteLayeringRecommendation(layeringId: String): Result<Unit> {
        return try {
            println("DEBUG - Deleting layering recommendation: $layeringId")
            client.from("layering_recommendations")
                .delete {
                    filter {
                        eq("id", layeringId)
                    }
                }
            println("DEBUG - Layering recommendation deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to delete layering recommendation: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getLayeringHistory(userId: String): Result<List<LayeringRecommendation>> {
        return try {
            println("DEBUG - Getting layering history for userId: $userId")
            val history = client.from("layering_recommendations")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<LayeringRecommendation>()
                .sortedByDescending { it.createdAt }
            println("DEBUG - Retrieved ${history.size} layering recommendations")
            Result.success(history)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to get layering history: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun saveInteraction(interaction: AIInteraction): Result<Unit> {
        return try {
            println("DEBUG - Saving AI interaction for userId: ${interaction.userId}")
            client.from("ai_interactions").insert(interaction)
            println("DEBUG - AI interaction saved")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to save interaction: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateInteractionFeedback(interactionId: String, wasHelpful: Boolean): Result<Unit> {
        return try {
            println("DEBUG - Updating interaction feedback: $interactionId -> $wasHelpful")
            client.from("ai_interactions")
                .update(mapOf("was_helpful" to wasHelpful)) {
                    filter {
                        eq("id", interactionId)
                    }
                }
            println("DEBUG - Interaction feedback updated")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to update interaction feedback: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getInteractionHistory(userId: String): Result<List<AIInteraction>> {
        return try {
            println("DEBUG - Getting interaction history for userId: $userId")
            val history = client.from("ai_interactions")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<AIInteraction>()
                .sortedByDescending { it.timestamp }
                .take(50)
            println("DEBUG - Retrieved ${history.size} AI interactions")
            Result.success(history)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Failed to get interaction history: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun analyzeUserPreferences(
        userId: String,
        perfumes: List<PerfumeCloud>
    ): AIPersonalization {
        println("DEBUG - Analyzing user preferences with AI for ${perfumes.size} perfumes")

        return try {
            val analysis = geminiHelper.analyzeUserPreferences(perfumes)

            println("DEBUG - AI Analysis complete: Style=${analysis.styleProfile}, Brands=${analysis.preferredBrands.size}, Notes=${analysis.preferredNotes.size}")

            AIPersonalization(
                userId = userId,
                preferredBrands = analysis.preferredBrands,
                preferredNotes = analysis.preferredNotes,
                commonOccasions = analysis.commonOccasions,
                styleProfile = analysis.styleProfile,
                intensityPreference = analysis.intensityPreference,
                lastUpdated = System.currentTimeMillis().toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - AI analysis failed, falling back to basic analysis: ${e.message}")

            // Fallback to basic frequency analysis
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

            AIPersonalization(
                userId = userId,
                preferredBrands = preferredBrands,
                preferredNotes = preferredNotes,
                commonOccasions = emptyMap(),
                styleProfile = "Balanced",
                intensityPreference = "Moderate",
                lastUpdated = System.currentTimeMillis().toString()
            )
        }
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