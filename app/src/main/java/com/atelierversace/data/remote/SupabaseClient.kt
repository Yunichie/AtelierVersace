package com.atelierversace.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import com.atelierversace.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class UserProfile(
    @SerialName("user_id")
    val id: String,

    val email: String,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("preferred_brands")
    val preferredBrands: List<String> = emptyList(),

    @SerialName("preferred_notes")
    val preferredNotes: List<String> = emptyList(),

    @SerialName("common_occasions")
    val commonOccasions: List<String> = emptyList(),

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class PerfumeCloud(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    val brand: String,
    val name: String,

    @SerialName("image_uri")
    val imageUri: String,

    val analogy: String,

    @SerialName("core_feeling")
    val coreFeeling: String,

    @SerialName("local_context")
    val localContext: String,

    @SerialName("top_notes")
    val topNotes: String,

    @SerialName("middle_notes")
    val middleNotes: String,

    @SerialName("base_notes")
    val baseNotes: String,

    @SerialName("is_wishlist")
    val isWishlist: Boolean,

    @SerialName("is_favorite")
    val isFavorite: Boolean = false,

    val timestamp: String
)

@Serializable
data class AIPersonalization(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("preferred_brands")
    val preferredBrands: List<String>,

    @SerialName("preferred_notes")
    val preferredNotes: List<String>,

    @SerialName("common_occasions")
    val commonOccasions: Map<String, Int>,

    @SerialName("style_profile")
    val styleProfile: String,

    @SerialName("intensity_preference")
    val intensityPreference: String,

    @SerialName("last_updated")
    val lastUpdated: String
)

@Serializable
data class LayeringRecommendation(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("base_perfume_id")
    val basePerfumeId: String,

    @SerialName("layer_perfume_id")
    val layerPerfumeId: String,

    val occasion: String,
    val reasoning: String,

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class AIInteraction(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("interaction_type")
    val interactionType: String,

    val query: String?,
    val result: String,

    @SerialName("was_helpful")
    val wasHelpful: Boolean? = null,

    val timestamp: String
)