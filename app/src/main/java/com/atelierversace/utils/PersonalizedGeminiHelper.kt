package com.atelierversace.utils

import com.atelierversace.data.remote.AIPersonalization
import com.atelierversace.data.remote.PerfumeCloud
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import com.atelierversace.BuildConfig
import org.json.JSONArray
import org.json.JSONObject

data class PreferenceAnalysis(
    val preferredBrands: List<String>,
    val preferredNotes: List<String>,
    val commonOccasions: Map<String, Int>,
    val styleProfile: String,
    val intensityPreference: String
)

class PersonalizedGeminiHelper {
    private val textModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_KEY
    )

    private fun cleanJsonResponse(response: String): String {
        var cleaned = response.trim()

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json").removeSuffix("```").trim()
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```").removeSuffix("```").trim()
        }

        val firstBrace = cleaned.indexOf('{')
        val firstBracket = cleaned.indexOf('[')

        if (firstBrace > 0 || firstBracket > 0) {
            val startIndex = when {
                firstBrace == -1 -> firstBracket
                firstBracket == -1 -> firstBrace
                else -> minOf(firstBrace, firstBracket)
            }
            cleaned = cleaned.substring(startIndex)
        }

        val lastBrace = cleaned.lastIndexOf('}')
        val lastBracket = cleaned.lastIndexOf(']')

        if (lastBrace != -1 || lastBracket != -1) {
            val endIndex = maxOf(lastBrace, lastBracket)
            if (endIndex != -1) {
                cleaned = cleaned.substring(0, endIndex + 1)
            }
        }

        return cleaned
    }

    suspend fun analyzeUserPreferences(perfumes: List<PerfumeCloud>): PreferenceAnalysis {
        try {
            val perfumeData = perfumes.joinToString("\n") {
                """
                Brand: ${it.brand}
                Name: ${it.name}
                Analogy: ${it.analogy}
                Feeling: ${it.coreFeeling}
                Context: ${it.localContext}
                Top: ${it.topNotes}
                Middle: ${it.middleNotes}
                Base: ${it.baseNotes}
                ---
                """.trimIndent()
            }

            val prompt = """
                Analyze this perfume collection and extract user preferences.
                
                COLLECTION:
                $perfumeData
                
                Based on the collection, determine:
                1. Top 5 preferred brands (by frequency and quality)
                2. Top 10 preferred notes (considering all note layers)
                3. Style profile (Fresh, Floral, Woody, Oriental, Fruity, Spicy, Aquatic, Gourmand, or combination)
                4. Intensity preference (Light, Moderate, Strong)
                5. Common occasions/contexts mentioned
                
                Respond with ONLY valid JSON (no markdown, no code blocks):
                {
                    "preferredBrands": ["Brand1", "Brand2", "Brand3", "Brand4", "Brand5"],
                    "preferredNotes": ["Note1", "Note2", "Note3", "Note4", "Note5", "Note6", "Note7", "Note8", "Note9", "Note10"],
                    "styleProfile": "Style or combination",
                    "intensityPreference": "Light/Moderate/Strong",
                    "commonOccasions": {
                        "casual": 5,
                        "formal": 3,
                        "evening": 2
                    }
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = cleanJsonResponse(response.text ?: throw Exception("Empty response"))

            println("DEBUG - Preference analysis response: $jsonText")

            val json = JSONObject(jsonText)

            val occasions = mutableMapOf<String, Int>()
            val occasionsJson = json.optJSONObject("commonOccasions")
            if (occasionsJson != null) {
                val keys = occasionsJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    occasions[key] = occasionsJson.getInt(key)
                }
            }

            return PreferenceAnalysis(
                preferredBrands = json.getJSONArray("preferredBrands").let { array ->
                    (0 until array.length()).map { array.getString(it) }
                },
                preferredNotes = json.getJSONArray("preferredNotes").let { array ->
                    (0 until array.length()).map { array.getString(it) }
                },
                commonOccasions = occasions,
                styleProfile = json.getString("styleProfile"),
                intensityPreference = json.getString("intensityPreference")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR in AI preference analysis: ${e.message}")
            throw e
        }
    }

    suspend fun generatePersonalizedRecommendation(
        wardrobe: List<PerfumeCloud>,
        personalization: AIPersonalization?,
        weather: WeatherData,
        occasion: String? = null
    ): Triple<PerfumeCloud, String, String>? {
        if (wardrobe.isEmpty()) return null

        try {
            val perfumeList = wardrobe.joinToString("\n") {
                "ID: ${it.id}\nBrand: ${it.brand}\nName: ${it.name}\nAnalogy: ${it.analogy}\nFeeling: ${it.coreFeeling}\nTop: ${it.topNotes}\nMiddle: ${it.middleNotes}\nBase: ${it.baseNotes}\n"
            }

            val personalizationContext = if (personalization != null) {
                """
                User Preferences:
                - Favorite Brands: ${personalization.preferredBrands.joinToString(", ")}
                - Preferred Notes: ${personalization.preferredNotes.joinToString(", ")}
                - Style: ${personalization.styleProfile}
                - Intensity: ${personalization.intensityPreference}
                """
            } else {
                "No personalization data available"
            }

            val occasionContext = occasion ?: "General daily wear"

            val prompt = """
                You are a personalized fragrance AI. Recommend ONE perfume from this wardrobe.
                
                WARDROBE:
                $perfumeList
                
                $personalizationContext
                
                CONTEXT:
                - Weather: ${weather.temperature}°C, ${weather.humidity}% humidity, ${weather.description}
                - Location: Surabaya, Indonesia (tropical)
                - Occasion: $occasionContext
                
                Respond with ONLY valid JSON (no markdown, no explanations, no code blocks):
                {
                    "perfumeId": "<exact id from list>",
                    "reason": "2-3 sentences explaining why this fits preferences and context",
                    "layeringId": "<id or 'none'>",
                    "layeringReason": "Brief layering explanation or 'Not recommended'"
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = cleanJsonResponse(response.text ?: return null)

            println("DEBUG - Recommendation response: $jsonText")

            val json = JSONObject(jsonText)

            val perfumeId = json.getString("perfumeId")
            val reason = json.getString("reason")
            val layeringId = json.optString("layeringId", "none")
            val layeringReason = json.optString("layeringReason", "")

            val selectedPerfume = wardrobe.find { it.id == perfumeId }
            if (selectedPerfume == null) {
                println("ERROR: Could not find perfume with ID: $perfumeId")
                return null
            }

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
            println("ERROR in recommendation: ${e.message}")
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
                USER PREFERENCES:
                - Favorite brands: ${personalization.preferredBrands.joinToString(", ")}
                - Preferred notes: ${personalization.preferredNotes.joinToString(", ")}
                - Style: ${personalization.styleProfile}
                - Intensity: ${personalization.intensityPreference}
                
                Prioritize perfumes matching these preferences.
                """
            } else {
                ""
            }

            val prompt = """
                Query: "$query"
                
                $personalizationContext
                
                Use Google Search to find 3-5 REAL perfumes that match this query.
                Include actual brand names, perfume names, and accurate note information.
                Focus on popular, currently available perfumes.
                
                Respond with ONLY valid JSON array (no markdown, no code blocks):
                [
                    {
                        "brand": "Real Brand Name",
                        "name": "Real Perfume Name",
                        "analogy": "Evocative comparison",
                        "coreFeeling": "2-3 feeling words",
                        "localContext": "Suitability for Surabaya (tropical, humid, 28-32°C)",
                        "topNotes": ["Note1", "Note2", "Note3"],
                        "middleNotes": ["Note1", "Note2", "Note3"],
                        "baseNotes": ["Note1", "Note2", "Note3"]
                    }
                ]
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = cleanJsonResponse(response.text ?: return emptyList())

            println("DEBUG - Discovery response: $jsonText")

            val jsonArray = JSONArray(jsonText)
            val profiles = mutableListOf<com.atelierversace.data.model.PersonaProfile>()

            for (i in 0 until jsonArray.length()) {
                try {
                    val json = jsonArray.getJSONObject(i)

                    profiles.add(
                        com.atelierversace.data.model.PersonaProfile(
                            brand = json.getString("brand"),
                            name = json.getString("name"),
                            analogy = json.getString("analogy"),
                            coreFeeling = json.getString("coreFeeling"),
                            localContext = json.getString("localContext"),
                            topNotes = json.getJSONArray("topNotes").let { array ->
                                (0 until array.length()).map { array.getString(it) }
                            },
                            middleNotes = json.getJSONArray("middleNotes").let { array ->
                                (0 until array.length()).map { array.getString(it) }
                            },
                            baseNotes = json.getJSONArray("baseNotes").let { array ->
                                (0 until array.length()).map { array.getString(it) }
                            }
                        )
                    )
                } catch (e: Exception) {
                    println("ERROR parsing profile $i: ${e.message}")
                    continue
                }
            }

            return profiles
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR in discovery: ${e.message}")
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
                "ID: ${it.id} | ${it.brand} ${it.name}\nTop: ${it.topNotes} | Middle: ${it.middleNotes} | Base: ${it.baseNotes}\n"
            }

            val personalizationContext = if (personalization != null) {
                "User prefers: ${personalization.styleProfile} style with ${personalization.preferredNotes.take(5).joinToString(", ")}"
            } else {
                "No preference data"
            }

            val prompt = """
                Analyze this wardrobe and suggest 3-5 layering combinations.
                
                WARDROBE:
                $perfumeList
                
                $personalizationContext
                
                Consider note harmony, complementary scents, and wearability.
                
                Respond with ONLY valid JSON array (no markdown, no code blocks):
                [
                    {
                        "baseId": "<id>",
                        "layerId": "<id>",
                        "name": "Creative name",
                        "description": "Why it works",
                        "occasion": "Best occasion",
                        "harmonyScore": 85
                    }
                ]
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = cleanJsonResponse(response.text ?: return emptyList())

            println("DEBUG - Layering response: $jsonText")

            val jsonArray = JSONArray(jsonText)
            val combinations = mutableListOf<LayeringCombination>()

            for (i in 0 until jsonArray.length()) {
                try {
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
                } catch (e: Exception) {
                    println("ERROR parsing combination $i: ${e.message}")
                    continue
                }
            }

            return combinations
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR in layering: ${e.message}")
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