package com.atelierversace.utils

import com.atelierversace.data.remote.AIPersonalization
import com.atelierversace.data.remote.PerfumeCloud
import com.atelierversace.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import org.json.JSONArray
import org.json.JSONObject

data class PreferenceAnalysis(
    val preferredBrands: List<String>,
    val preferredNotes: List<String>,
    val commonOccasions: Map<String, Int>,
    val styleProfile: String,
    val intensityPreference: String
)

data class LayeringCombination(
    val basePerfume: PerfumeCloud,
    val layerPerfume: PerfumeCloud,
    val name: String,
    val description: String,
    val occasion: String,
    val harmonyScore: Int
)

class PersonalizedGeminiHelper {

    private val analyticsModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-2.0-flash",
        generationConfig = generationConfig {
            temperature = 0.3f
            topK = 20
            topP = 0.8f
            maxOutputTokens = 8192
        }
    )

    private val recommendationModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-2.0-flash",
        generationConfig = generationConfig {
            temperature = 0.6f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 8192
        }
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
            val perfumeData = perfumes.joinToString("\n\n") { perfume ->
                """
                PERFUME ${perfumes.indexOf(perfume) + 1}:
                Brand: ${perfume.brand}
                Name: ${perfume.name}
                Analogy: ${perfume.analogy}
                Core Feeling: ${perfume.coreFeeling}
                Local Context: ${perfume.localContext}
                Top Notes: ${perfume.topNotes}
                Middle Notes: ${perfume.middleNotes}
                Base Notes: ${perfume.baseNotes}
                """.trimIndent()
            }

            val prompt = """
                You are a perfume data analyst. Analyze this user's perfume collection to extract their preferences and create a detailed profile.
                
                COLLECTION (${perfumes.size} perfumes):
                $perfumeData
                
                ANALYSIS REQUIREMENTS:
                1. PREFERRED BRANDS: Identify top 5 brands by frequency and quality indicators
                2. PREFERRED NOTES: Extract the 10 most common notes across ALL layers (top, middle, base)
                3. STYLE PROFILE: Determine the dominant fragrance family/families (Fresh, Floral, Woody, Oriental, Fruity, Spicy, Aquatic, Gourmand, Chypre, Fougère, or combinations)
                4. INTENSITY PREFERENCE: Assess if they prefer Light, Moderate, or Strong fragrances based on notes and descriptions
                5. COMMON OCCASIONS: Extract mentioned occasions/contexts from local context field
                
                ANALYSIS DEPTH:
                - Look for patterns in note choices
                - Consider the analogy and feeling descriptions
                - Identify any clear preferences (e.g., citrus-heavy, floral-forward)
                - Note any climate-specific patterns
                
                Return ONLY a JSON object (no markdown, no explanation):
                {
                    "preferredBrands": ["Brand1", "Brand2", "Brand3", "Brand4", "Brand5"],
                    "preferredNotes": ["Note1", "Note2", "Note3", "Note4", "Note5", "Note6", "Note7", "Note8", "Note9", "Note10"],
                    "styleProfile": "Primary Style / Secondary Style (e.g., 'Fresh Citrus / Woody Oriental')",
                    "intensityPreference": "Light/Moderate/Strong",
                    "commonOccasions": {
                        "evening": 5,
                        "casual": 3,
                        "formal": 2,
                        "daytime": 4
                    }
                }
            """.trimIndent()

            val response = analyticsModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response")
            val jsonText = cleanJsonResponse(responseText)

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
            val perfumeList = wardrobe.mapIndexed { index, perfume ->
                """
                [${index + 1}] ID: ${perfume.id}
                ${perfume.brand} ${perfume.name}
                Analogy: ${perfume.analogy}
                Core Feeling: ${perfume.coreFeeling}
                Local Context: ${perfume.localContext}
                Top Notes: ${perfume.topNotes}
                Middle Notes: ${perfume.middleNotes}
                Base Notes: ${perfume.baseNotes}
                Favorite: ${if (perfume.isFavorite) "YES" else "No"}
                """.trimIndent()
            }.joinToString("\n\n")

            val personalizationContext = if (personalization != null) {
                """
                USER PREFERENCES (LEARNED FROM COLLECTION):
                - Favorite Brands: ${personalization.preferredBrands.joinToString(", ")}
                - Preferred Notes: ${personalization.preferredNotes.joinToString(", ")}
                - Style Profile: ${personalization.styleProfile}
                - Intensity Preference: ${personalization.intensityPreference}
                - Common Occasions: ${personalization.commonOccasions.entries.sortedByDescending { it.value }.take(3).joinToString(", ") { "${it.key} (${it.value}x)" }}
                
                PRIORITY: Give strong preference to perfumes matching their preferred brands, notes, and style.
                """
            } else {
                "USER PREFERENCES: Not yet established (this is a new collection)"
            }

            val occasionContext = occasion ?: "General daily wear / flexible occasion"

            val prompt = """
                You are a personal AI fragrance stylist. Recommend ONE perfume from this user's wardrobe.
                
                WARDROBE (${wardrobe.size} perfumes):
                $perfumeList
                
                $personalizationContext
                
                CURRENT CONDITIONS:
                - Temperature: ${weather.temperature}°C
                - Humidity: ${weather.humidity}%
                - Weather: ${weather.description}
                - Location: Surabaya, Indonesia (tropical coastal city)
                - Occasion: $occasionContext
                
                RECOMMENDATION CRITERIA:
                1. **User Preferences** (40%): Match their preferred brands, notes, and style
                2. **Weather Suitability** (30%): Consider temperature, humidity, and tropical climate
                3. **Occasion Appropriateness** (20%): Match the event/context
                4. **Seasonal Performance** (10%): How notes perform in current conditions
                
                SPECIAL CONSIDERATIONS:
                - In high humidity, fresh/citrus notes project well
                - Heavy orientals may be overwhelming in heat
                - Consider if user marked any perfumes as favorites
                - Tropical climate affects sillage and longevity
                
                LAYERING ANALYSIS:
                If beneficial, suggest ONE perfume to layer with the main recommendation.
                Only suggest layering if:
                - Notes are complementary (not competing)
                - Combined effect enhances the experience
                - Suitable for the weather conditions
                
                Return ONLY a JSON object (no markdown, no explanation):
                {
                    "perfumeId": "<exact id from list>",
                    "reason": "3-4 sentences explaining why this perfume is perfect. Mention: how it matches their preferences, why it works in current weather, and what makes it ideal for the occasion. Be specific about notes.",
                    "layeringId": "<id or 'none'>",
                    "layeringReason": "If layering recommended, explain the note harmony and combined effect. If not, say 'Not recommended for these conditions'"
                }
            """.trimIndent()

            val response = recommendationModel.generateContent(prompt)
            val responseText = response.text ?: return null
            val jsonText = cleanJsonResponse(responseText)

            println("DEBUG - Personalized recommendation response: $jsonText")

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

            val layeringSuggestion = if (layeringId != "none" && layeringId.isNotEmpty()) {
                val layerPerfume = wardrobe.find { it.id == layeringId }
                if (layerPerfume != null) {
                    "💫 Layering Suggestion:\n${layerPerfume.brand} ${layerPerfume.name}\n\n$layeringReason"
                } else {
                    "No layering recommended"
                }
            } else {
                "No layering recommended"
            }

            return Triple(selectedPerfume, reason, layeringSuggestion)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR in personalized recommendation: ${e.message}")
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
                USER PREFERENCES (LEARNED PROFILE):
                - Favorite Brands: ${personalization.preferredBrands.joinToString(", ")}
                - Preferred Notes: ${personalization.preferredNotes.joinToString(", ")}
                - Style Profile: ${personalization.styleProfile}
                - Intensity Preference: ${personalization.intensityPreference}
                
                INSTRUCTION: Prioritize recommendations that align with these preferences while still matching the query.
                If possible, suggest perfumes from their favorite brands or with their preferred notes.
                """
            } else {
                "USER PREFERENCES: No learned profile yet (suggest diverse options)"
            }

            val prompt = """
                You are a perfume discovery expert. Based on the user's query and preferences, recommend 4-5 REAL perfumes.
                
                USER QUERY: "$query"
                
                $personalizationContext
                
                LOCATION CONTEXT: Surabaya, Indonesia (tropical, 28-32°C, 70-80% humidity)
                
                REQUIREMENTS:
                1. Recommend REAL, commercially available perfumes
                2. Use accurate brand and product names
                3. Match the user's query description
                4. Consider their learned preferences (if available)
                5. Provide accurate note information
                6. Ensure climate suitability
                7. Mix well-known and niche options
                8. Ensure diversity in recommendations
                
                QUALITY STANDARDS:
                - Only recommend perfumes that actually exist in the market
                - Use proper brand names (e.g., "Chanel", "Dior", "Tom Ford", "Jo Malone", "Hermès", "Maison Margiela")
                - Use real perfume names (e.g., "Sauvage", "Santal 33", "Oud Wood", "English Pear & Freesia")
                - Notes should be accurate if you know the perfume
                - Avoid heavy/cloying scents for tropical climate
                
                Return ONLY a JSON array (no markdown, no explanation):
                [
                    {
                        "brand": "Real Brand Name",
                        "name": "Real Perfume Name",
                        "analogy": "Vivid, sensory-rich comparison that captures the essence",
                        "coreFeeling": "2-3 descriptive words",
                        "localContext": "Specific advice for Surabaya's tropical climate (be detailed about performance)",
                        "topNotes": ["Note1", "Note2", "Note3"],
                        "middleNotes": ["Note1", "Note2", "Note3"],
                        "baseNotes": ["Note1", "Note2", "Note3"]
                    }
                ]
            """.trimIndent()

            val response = recommendationModel.generateContent(prompt)
            val responseText = response.text ?: return emptyList()
            val jsonText = cleanJsonResponse(responseText)

            println("DEBUG - Personalized discovery response: $jsonText")

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
                    println("ERROR parsing discovery profile $i: ${e.message}")
                    continue
                }
            }

            return profiles
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR in personalized discovery: ${e.message}")
            return emptyList()
        }
    }

    suspend fun generateLayeringCombinations(
        wardrobe: List<PerfumeCloud>,
        personalization: AIPersonalization?
    ): List<LayeringCombination> {
        if (wardrobe.size < 2) return emptyList()

        try {
            val perfumeList = wardrobe.mapIndexed { index, perfume ->
                """
                [${index + 1}] ID: ${perfume.id}
                ${perfume.brand} ${perfume.name}
                Core Feeling: ${perfume.coreFeeling}
                Top Notes: ${perfume.topNotes}
                Middle Notes: ${perfume.middleNotes}
                Base Notes: ${perfume.baseNotes}
                """.trimIndent()
            }.joinToString("\n\n")

            val personalizationContext = if (personalization != null) {
                """
                USER PREFERENCES:
                - Style Profile: ${personalization.styleProfile}
                - Preferred Notes: ${personalization.preferredNotes.take(5).joinToString(", ")}
                - Intensity Preference: ${personalization.intensityPreference}
                
                Consider these preferences when creating combinations.
                """
            } else {
                "No preference data available. Focus on universal appeal."
            }

            val prompt = """
                You are a perfume layering expert. Analyze this wardrobe and suggest 3-5 harmonious layering combinations.
                
                WARDROBE (${wardrobe.size} perfumes):
                $perfumeList
                
                $personalizationContext
                
                LAYERING PRINCIPLES:
                1. **Note Harmony** (40%): Complementary notes that don't compete
                2. **Intensity Balance** (25%): One should be subtle, one more prominent
                3. **Occasion Suitability** (20%): Combined effect should fit specific contexts
                4. **Tropical Climate** (15%): Work well in Surabaya's heat and humidity
                
                QUALITY CRITERIA:
                - Base perfume should be the foundation (typically woody/oriental/amber)
                - Layer perfume should enhance (typically fresh/floral/citrus)
                - Combined scent should be cohesive, not chaotic
                - Avoid clashing notes (e.g., heavy oud + sweet vanilla)
                - Consider wearability and mass appeal
                
                CREATIVE NAMING:
                Give each combination an evocative name that captures the combined essence.
                
                Return ONLY a JSON array (no markdown, no explanation):
                [
                    {
                        "baseId": "<id of base perfume>",
                        "layerId": "<id of layer perfume>",
                        "name": "Creative, evocative name for the combination",
                        "description": "2-3 sentences explaining the note harmony, why it works, and the resulting character",
                        "occasion": "Best occasion/context for this combination",
                        "harmonyScore": 85
                    }
                ]
                
                Harmony score: 90-100 (exceptional), 80-89 (very good), 70-79 (good), below 70 (don't recommend)
            """.trimIndent()

            val response = recommendationModel.generateContent(prompt)
            val responseText = response.text ?: return emptyList()
            val jsonText = cleanJsonResponse(responseText)

            println("DEBUG - Layering combinations response: $jsonText")

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
                    } else {
                        println("WARNING: Could not find perfumes for combination: base=$baseId, layer=$layerId")
                    }
                } catch (e: Exception) {
                    println("ERROR parsing layering combination $i: ${e.message}")
                    continue
                }
            }

            return combinations
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR generating layering combinations: ${e.message}")
            return emptyList()
        }
    }
}