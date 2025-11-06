package com.atelierversace.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import com.atelierversace.BuildConfig

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}

data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val preferredBrands: List<String> = emptyList(),
    val preferredNotes: List<String> = emptyList(),
    val commonOccasions: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

data class PerfumeCloud(
    val id: String? = null,
    val userId: String,
    val brand: String,
    val name: String,
    val imageUri: String,
    val analogy: String,
    val coreFeeling: String,
    val localContext: String,
    val topNotes: String,
    val middleNotes: String,
    val baseNotes: String,
    val isWishlist: Boolean,
    val isFavorite: Boolean = false,
    val timestamp: String
)

data class AIPersonalization(
    val id: String? = null,
    val userId: String,
    val preferredBrands: List<String>,
    val preferredNotes: List<String>,
    val commonOccasions: Map<String, Int>,
    val styleProfile: String,
    val intensityPreference: String,
    val lastUpdated: String
)

data class LayeringRecommendation(
    val id: String? = null,
    val userId: String,
    val basePerfumeId: String,
    val layerPerfumeId: String,
    val occasion: String,
    val reasoning: String,
    val createdAt: String
)

data class AIInteraction(
    val id: String? = null,
    val userId: String,
    val interactionType: String,
    val query: String?,
    val result: String,
    val wasHelpful: Boolean? = null,
    val timestamp: String
)