package com.atelierversace.utils

import com.atelierversace.data.remote.AIPersonalization
import com.atelierversace.data.remote.PerfumeCloud
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import com.atelierversace.BuildConfig
import org.json.JSONArray
import org.json.JSONObject

class PersonalizedGeminiHelper {
    private val textModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_KEY
    )

    suspend fun generatePersonalizedRecommendation(
        wardrobe: List<PerfumeCloud>,
        personalization: AIPersonalization?,
        weather: WeatherData,
        occasion: String? = null
    ): Triple<PerfumeCloud, String, String>? {
        if (wardrobe.isEmpty()) return null

        try {
            val perfumeList = wardrobe.joinToString("\n") {
                "${it.id}. ${it.brand} ${it.name} - ${it.analogy} (${it.coreFeeling})\n" +
                        "   Top: ${it.topNotes} | Middle: ${it.middleNotes} | Base: ${it.baseNotes}"
            }

            val personalizationContext = if (personalization != null) {
                """
                User Preferences:
                - Favorite Brands: ${personalization.preferredBrands.joinToString(", ")}
                - Preferred Notes: ${personalization.preferredNotes.joinToString(", ")}
                - Style Profile: ${personalization.styleProfile}
                - Intensity Preference: ${personalization.intensityPreference}
                """
            } else {
                "No personalization data yet - making general recommendation"
            }

            val occasionContext = if (occasion != null) {
                "Occasion: $occasion"
            } else {
                "General daily wear"
            }

            val prompt = """
                You are a personalized AI fragrance stylist. Recommend ONE perfume from the user's wardrobe.
                
                User's Wardrobe:
                $perfumeList
                
                $personalizationContext
                
                Current Context:
                - Weather: ${weather.temperature}°C, ${weather.humidity}% humidity, ${weather.description}
                - Location: Surabaya, Indonesia (tropical, humid)
                - $occasionContext
                
                Consider:
                1. User's established preferences (brands, notes, style)
                2. Weather suitability for tropical climate
                3. Occasion appropriateness
                4. Recent usage patterns (avoid repeating too often)
                
                Also suggest ONE complementary perfume from their wardrobe for layering (if suitable), or "none" if layering isn't recommended.
                
                Respond ONLY with JSON WITHOUT codeblock:
                {
                    "perfumeId": "<id>",
                    "reason": "2-3 sentences explaining why this matches their preferences and context",
                    "layeringId": "<id or 'none'>",
                    "layeringReason": "Brief explanation of why this layering works, or 'Not recommended' if none"
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = response.text?.trim() ?: return null
            val json = JSONObject(jsonText)

            val perfumeId = json.getString("perfumeId")
            val reason = json.getString("reason")
            val layeringId = json.optString("layeringId", "none")
            val layeringReason = json.optString("layeringReason", "")

            val selectedPerfume = wardrobe.find { it.id == perfumeId } ?: return null

            val layeringSuggestion = if (layeringId != "none") {
                val layerPerfume = wardrobe.find { it.id == layeringId }
                if (layerPerfume != null) {
                    "Layer with: ${layerPerfume.brand} ${layerPerfume.name}\n$layeringReason"
                } else {
                    "No layering recommended"
                }
            } else {
                "No layering recommended"
            }

            return Triple(selectedPerfume, reason, layeringSuggestion)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun discoverPersonalizedPerfumes(
        query: String,
        personalization: AIPersonalization?
    ): List<com.atelierversace.data.model.PersonaProfile> {
        try {
            val personalizationContext = if (personalization != null) {
                """
                Consider the user's preferences:
                - They love brands like: ${personalization.preferredBrands.joinToString(", ")}
                - They prefer notes like: ${personalization.preferredNotes.joinToString(", ")}
                - Their style is: ${personalization.styleProfile}
                - Intensity preference: ${personalization.intensityPreference}
                
                Suggest perfumes that align with these preferences while matching the query.
                """
            } else {
                ""
            }

            val prompt = """
                Based on this query: "$query"
                
                $personalizationContext
                
                Use Google Search to find 3-5 perfumes that match.
                Prioritize real, available perfumes that fit the user's taste profile.
                Include actual brand names, perfume names, and complete note breakdowns.
                
                Respond ONLY with JSON array WITHOUT codeblock:
                [
                    {
                        "brand": "Brand Name",
                        "name": "Perfume Name",
                        "analogy": "Evocative analogy",
                        "coreFeeling": "Feeling words",
                        "localContext": "Suitability for Surabaya climate",
                        "topNotes": ["Note1", "Note2", "Note3"],
                        "middleNotes": ["Note1", "Note2", "Note3"],
                        "baseNotes": ["Note1", "Note2", "Note3"]
                    }
                ]
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = response.text?.trim() ?: return emptyList()
            val jsonArray = JSONArray(jsonText)
            val profiles = mutableListOf<com.atelierversace.data.model.PersonaProfile>()

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                val topNotesArray = json.getJSONArray("topNotes")
                val middleNotesArray = json.getJSONArray("middleNotes")
                val baseNotesArray = json.getJSONArray("baseNotes")

                profiles.add(
                    com.atelierversace.data.model.PersonaProfile(
                        brand = json.getString("brand"),
                        name = json.getString("name"),
                        analogy = json.getString("analogy"),
                        coreFeeling = json.getString("coreFeeling"),
                        localContext = json.getString("localContext"),
                        topNotes = (0 until topNotesArray.length()).map { topNotesArray.getString(it) },
                        middleNotes = (0 until middleNotesArray.length()).map { middleNotesArray.getString(it) },
                        baseNotes = (0 until baseNotesArray.length()).map { baseNotesArray.getString(it) }
                    )
                )
            }

            return profiles
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun generateLayeringCombinations(
        wardrobe: List<PerfumeCloud>,
        personalization: AIPersonalization?
    ): List<LayeringCombination> {
        if (wardrobe.size < 2) return emptyList()

        try {
            val perfumeList = wardrobe.joinToString("\n") {
                "${it.id}. ${it.brand} ${it.name}\n" +
                        "   Top: ${it.topNotes} | Middle: ${it.middleNotes} | Base: ${it.baseNotes}"
            }

            val personalizationContext = if (personalization != null) {
                "User prefers: ${personalization.styleProfile} style with ${personalization.preferredNotes.take(5).joinToString(", ")} notes"
            } else {
                "No preference data"
            }

            val prompt = """
                Analyze this perfume wardrobe and suggest 3-5 creative layering combinations.
                
                $perfumeList
                
                $personalizationContext
                
                Consider:
                - Note harmony and complementary scents
                - Base + accent layering principles
                - User's style preferences
                - Practical wearability
                
                Respond with JSON array WITHOUT codeblock:
                [
                    {
                        "baseId": "<id>",
                        "layerId": "<id>",
                        "name": "Creative combination name",
                        "description": "Why this works",
                        "occasion": "Best for this occasion",
                        "harmonyScore": 0-100
                    }
                ]
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = response.text?.trim() ?: return emptyList()
            val jsonArray = JSONArray(jsonText)
            val combinations = mutableListOf<LayeringCombination>()

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                val baseId = json.getString("baseId")
                val layerId = json.getString("layerId")

                val basePerfume = wardrobe.find { it.id == baseId }
                val layerPerfume = wardrobe.find { it.id == layerId }

                if (basePerfume != null && layerPerfume != null) {
                    combinations.add(
                        LayeringCombination(
                            basePerfume = basePerfume,
                            layerPerfume = layerPerfume,
                            name = json.getString("name"),
                            description = json.getString("description"),
                            occasion = json.getString("occasion"),
                            harmonyScore = json.optInt("harmonyScore", 80)
                        )
                    )
                }
            }

            return combinations
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}

data class LayeringCombination(
    val basePerfume: PerfumeCloud,
    val layerPerfume: PerfumeCloud,
    val name: String,
    val description: String,
    val occasion: String,
    val harmonyScore: Int
)