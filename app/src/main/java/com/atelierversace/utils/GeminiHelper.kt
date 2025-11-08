package com.atelierversace.utils

import android.graphics.Bitmap
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.data.model.Perfume
import org.json.JSONObject
import org.json.JSONArray
import com.atelierversace.BuildConfig
import androidx.core.graphics.scale
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

class GeminiHelper {

    private val visionModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-2.0-flash",
        generationConfig = generationConfig {
            temperature = 0.4f
            topK = 32
            topP = 1f
            maxOutputTokens = 8192
        }
    )

    private val textModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-2.0-flash",
        generationConfig = generationConfig {
            temperature = 0.7f
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

    private fun optimizeImageForAI(originalBitmap: Bitmap): Bitmap {
        try {
            val maxDimension = 2048
            val scale = if (originalBitmap.width > originalBitmap.height) {
                maxDimension.toFloat() / originalBitmap.width
            } else {
                maxDimension.toFloat() / originalBitmap.height
            }

            val scaledBitmap = if (scale < 1.0f) {
                val newWidth = (originalBitmap.width * scale).toInt()
                val newHeight = (originalBitmap.height * scale).toInt()
                originalBitmap.scale(newWidth, newHeight)
            } else {
                originalBitmap
            }

            println("DEBUG - Image optimization: Original=${originalBitmap.width}x${originalBitmap.height}, Scaled=${scaledBitmap.width}x${scaledBitmap.height}")

            return scaledBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR - Image optimization failed: ${e.message}")
            throw e
        }
    }

    suspend fun identifyPerfume(imageBitmap: Bitmap): Pair<String, String>? {
        try {
            val optimizedBitmap = optimizeImageForAI(imageBitmap)

            val prompt = """
                You are a perfume identification expert. Analyze this perfume bottle image with extreme precision.
                
                INSTRUCTIONS:
                1. Look carefully at ALL text on the bottle, label, and packaging
                2. The BRAND NAME is usually at the top or most prominent
                3. The PERFUME NAME is typically below the brand or on the main label
                4. Read EXACTLY what you see - do not guess or infer
                5. If you cannot clearly read the text, respond with "Unknown" for that field
                
                IMPORTANT RULES:
                - Only return text you can actually READ from the image
                - Do not make assumptions about the perfume based on bottle shape
                - Be conservative - if unsure, use "Unknown"
                
                Return ONLY a JSON object with this exact format (no markdown, no explanation):
                {
                    "brand": "Exact Brand Name As Written",
                    "name": "Exact Perfume Name As Written"
                }
                
                If you cannot read either field clearly, respond with:
                {
                    "brand": "Unknown",
                    "name": "Perfume"
                }
            """.trimIndent()

            val response = visionModel.generateContent(
                content {
                    image(optimizedBitmap)
                    text(prompt)
                }
            )

            val responseText = response.text ?: return null
            val jsonText = cleanJsonResponse(responseText)
            println("DEBUG - Identify response: $jsonText")

            val json = JSONObject(jsonText)

            val brand = json.getString("brand")
            val name = json.getString("name")

            println("DEBUG - Identified perfume: $brand - $name")

            if (brand == "Unknown" && name == "Perfume") {
                println("WARNING - Could not identify perfume from image")
                return null
            }

            return Pair(brand, name)
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR identifying perfume: ${e.message}")
            return null
        }
    }

    suspend fun generatePersonaProfile(brand: String, name: String): PersonaProfile {
        try {
            val prompt = """
                You are a fragrance expert with access to comprehensive perfume databases. Create an accurate, detailed persona profile for:
                
                PERFUME: $brand $name
                LOCATION CONTEXT: Surabaya, Indonesia (tropical climate, 28-32°C average, 70-80% humidity)
                
                REQUIREMENTS:
                1. Research this SPECIFIC perfume - use exact note information if available
                2. If this is a well-known perfume, provide ACCURATE notes based on real data
                3. Create an evocative analogy that captures the essence
                4. Describe the core feeling in 2-3 words
                5. Provide context specifically for Surabaya's tropical, humid climate
                6. List 3-4 notes for each layer (top, middle, base)
                
                QUALITY STANDARDS:
                - Accuracy is critical - if you know the perfume, use real information
                - The analogy should be vivid and memorable
                - Local context must address tropical humidity and heat
                - Notes should be specific and realistic
                
                Return ONLY a JSON object (no markdown, no explanation):
                {
                    "brand": "$brand",
                    "name": "$name",
                    "analogy": "A vivid, sensory-rich comparison (e.g., 'Like walking through a rain-soaked jasmine garden at twilight')",
                    "coreFeeling": "2-3 descriptive words (e.g., 'Fresh & Luminous')",
                    "localContext": "Specific advice for Surabaya's tropical climate (e.g., 'Best worn in air-conditioned spaces; the citrus notes shine in humidity')",
                    "topNotes": ["Note1", "Note2", "Note3"],
                    "middleNotes": ["Note1", "Note2", "Note3"],
                    "baseNotes": ["Note1", "Note2", "Note3"]
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response")
            val jsonText = cleanJsonResponse(responseText)

            println("DEBUG - Persona profile response: $jsonText")

            val json = JSONObject(jsonText)

            return PersonaProfile(
                brand = json.optString("brand", brand),
                name = json.optString("name", name),
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
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR generating persona profile: ${e.message}")

            return PersonaProfile(
                brand = brand,
                name = name,
                analogy = "A sophisticated and versatile fragrance that adapts to your presence",
                coreFeeling = "Elegant & Refined",
                localContext = "Suitable for various occasions in Surabaya's tropical climate. Best worn during cooler evening hours or in air-conditioned environments.",
                topNotes = listOf("Citrus", "Bergamot", "Fresh Accord"),
                middleNotes = listOf("Jasmine", "Rose", "Floral Heart"),
                baseNotes = listOf("Musk", "Cedarwood", "Amber")
            )
        }
    }

    suspend fun discoverPerfumes(query: String): List<PersonaProfile> {
        try {
            val prompt = """
                You are a perfume recommendation expert. Based on this user query, recommend 4-5 REAL, commercially available perfumes.
                
                USER QUERY: "$query"
                LOCATION: Surabaya, Indonesia (tropical, 28-32°C, humid)
                
                REQUIREMENTS:
                1. Recommend REAL perfumes with accurate brand and product names
                2. Choose perfumes that match the query description
                3. Include accurate note information (research if you know the perfume)
                4. Consider tropical climate suitability
                5. Mix popular and niche options
                6. Ensure diversity in recommendations
                
                QUALITY STANDARDS:
                - Only recommend perfumes that actually exist
                - Use accurate brand names (e.g., "Chanel", "Dior", "Jo Malone", "Tom Ford")
                - Provide real perfume names (e.g., "Sauvage", "Bloom", "Wood Sage & Sea Salt")
                - Notes should be accurate if you know the perfume
                - Consider climate appropriateness (avoid heavy/cloying scents)
                
                Return ONLY a JSON array (no markdown, no explanation):
                [
                    {
                        "brand": "Real Brand Name",
                        "name": "Real Perfume Name",
                        "analogy": "Vivid, sensory comparison",
                        "coreFeeling": "2-3 feeling words",
                        "localContext": "Specific advice for tropical Surabaya climate",
                        "topNotes": ["Note1", "Note2", "Note3"],
                        "middleNotes": ["Note1", "Note2", "Note3"],
                        "baseNotes": ["Note1", "Note2", "Note3"]
                    }
                ]
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val responseText = response.text ?: return emptyList()
            val jsonText = cleanJsonResponse(responseText)

            println("DEBUG - Discovery response: $jsonText")

            val jsonArray = JSONArray(jsonText)
            val profiles = mutableListOf<PersonaProfile>()

            for (i in 0 until jsonArray.length()) {
                try {
                    val json = jsonArray.getJSONObject(i)

                    profiles.add(
                        PersonaProfile(
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
            println("ERROR discovering perfumes: ${e.message}")
            return emptyList()
        }
    }

    suspend fun recommendPerfume(
        perfumes: List<Perfume>,
        weather: WeatherData,
        occasion: String
    ): Pair<Perfume, String>? {
        if (perfumes.isEmpty()) return null

        try {
            val perfumeList = perfumes.joinToString("\n") { perfume ->
                """
                ID: ${perfume.id}
                ${perfume.brand} ${perfume.name}
                Analogy: ${perfume.analogy}
                Feeling: ${perfume.coreFeeling}
                Context: ${perfume.localContext}
                Top: ${perfume.topNotes}
                Middle: ${perfume.middleNotes}
                Base: ${perfume.baseNotes}
                ---
                """.trimIndent()
            }

            val prompt = """
                You are a personal fragrance consultant. Recommend ONE perfume from this wardrobe that best fits the context.
                
                AVAILABLE PERFUMES:
                $perfumeList
                
                CURRENT CONTEXT:
                - Weather: ${weather.temperature}°C, ${weather.humidity}% humidity, ${weather.description}
                - Occasion: $occasion
                - Location: Surabaya, Indonesia (tropical)
                
                RECOMMENDATION CRITERIA:
                1. Weather appropriateness (consider temperature and humidity)
                2. Occasion suitability
                3. Note performance in tropical climate
                4. Overall harmony with context
                
                Return ONLY a JSON object (no markdown, no explanation):
                {
                    "perfumeId": <numeric id>,
                    "reason": "2-3 sentences explaining why this perfume is perfect for the weather and occasion, mentioning specific notes or characteristics"
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val responseText = response.text ?: return null
            val jsonText = cleanJsonResponse(responseText)

            println("DEBUG - Recommendation response: $jsonText")

            val json = JSONObject(jsonText)

            val perfumeId = json.getInt("perfumeId")
            val reason = json.getString("reason")

            val selectedPerfume = perfumes.find { it.id == perfumeId }

            return selectedPerfume?.let { it to reason }
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR in recommendation: ${e.message}")

            return perfumes.firstOrNull()?.let {
                it to "A versatile choice that works well for today's conditions."
            }
        }
    }

    suspend fun recommendPerfumeWithQuery(
        perfumes: List<Perfume>,
        weather: WeatherData,
        userQuery: String
    ): Pair<Perfume, String>? {
        if (perfumes.isEmpty()) return null

        try {
            val perfumeList = perfumes.joinToString("\n") { perfume ->
                """
                ID: ${perfume.id}
                ${perfume.brand} ${perfume.name}
                Analogy: ${perfume.analogy}
                Feeling: ${perfume.coreFeeling}
                Context: ${perfume.localContext}
                Top: ${perfume.topNotes}
                Middle: ${perfume.middleNotes}
                Base: ${perfume.baseNotes}
                ---
                """.trimIndent()
            }

            val prompt = """
                You are a personal fragrance consultant. The user has a specific request.
                
                USER REQUEST: "$userQuery"
                
                AVAILABLE PERFUMES:
                $perfumeList
                
                CURRENT CONTEXT:
                - Weather: ${weather.temperature}°C, ${weather.humidity}% humidity, ${weather.description}
                - Location: Surabaya, Indonesia (tropical climate)
                
                TASK:
                Recommend ONE perfume that best matches the user's request while considering the weather.
                
                CRITERIA:
                1. Match the user's described mood/occasion
                2. Consider weather appropriateness
                3. Explain how the perfume fulfills their request
                4. Mention specific notes that align with their needs
                
                Return ONLY a JSON object (no markdown, no explanation):
                {
                    "perfumeId": <numeric id>,
                    "reason": "3-4 sentences explaining how this perfume matches their request and the current conditions"
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val responseText = response.text ?: return null
            val jsonText = cleanJsonResponse(responseText)

            println("DEBUG - Query recommendation response: $jsonText")

            val json = JSONObject(jsonText)

            val perfumeId = json.getInt("perfumeId")
            val reason = json.getString("reason")

            val selectedPerfume = perfumes.find { it.id == perfumeId }

            return selectedPerfume?.let { it to reason }
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR in query recommendation: ${e.message}")
            return null
        }
    }
}