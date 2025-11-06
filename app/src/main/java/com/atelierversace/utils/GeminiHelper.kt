package com.atelierversace.utils

import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.data.model.Perfume
import org.json.JSONObject
import org.json.JSONArray
import com.atelierversace.BuildConfig
import dev.shreyaspatil.ai.client.generativeai.type.PlatformImage

class GeminiHelper {

    private val visionModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_KEY
    )

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

    suspend fun identifyPerfume(imageBytes: ByteArray): Pair<String, String>? {
        try {
            val prompt = """
                Identify this perfume from the image. Look at the bottle, label, and any text visible.
                You must respond with ONLY a valid JSON object (no markdown, no code blocks, no explanations).
                
                Format:
                {"brand": "Brand Name", "name": "Perfume Name"}
                
                If you cannot identify the perfume clearly:
                {"brand": "Unknown", "name": "Perfume"}
            """.trimIndent()

            val response = visionModel.generateContent(
                content {
                    PlatformImage(imageBytes)
                    text(prompt)
                }
            )

            val jsonText = cleanJsonResponse(response.text ?: return null)
            println("DEBUG - Identify response: $jsonText")

            val json = JSONObject(jsonText)

            val brand = json.getString("brand")
            val name = json.getString("name")

            println("DEBUG - Identified perfume: $brand - $name")
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
                Create a detailed persona profile for: $brand $name
                
                Context: Surabaya, Indonesia (tropical, humid climate, 28-32°C average).
                
                Search for actual information about this perfume using Google Search.
                Provide accurate notes and descriptions based on real data.
                
                Respond with ONLY valid JSON (no markdown, no code blocks):
                {
                    "brand": "$brand",
                    "name": "$name",
                    "analogy": "A vivid, evocative comparison (e.g., 'Like walking through a rain-soaked garden at dawn')",
                    "coreFeeling": "2-3 words describing the essence (e.g., 'Fresh & Invigorating')",
                    "localContext": "Specific advice for Surabaya's climate (e.g., 'Best for evening wear in air-conditioned spaces')",
                    "topNotes": ["Note1", "Note2", "Note3"],
                    "middleNotes": ["Note1", "Note2", "Note3"],
                    "baseNotes": ["Note1", "Note2", "Note3"]
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = cleanJsonResponse(response.text ?: throw Exception("Empty response"))

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
                analogy = "A sophisticated and versatile fragrance",
                coreFeeling = "Elegant & Refined",
                localContext = "Suitable for various occasions in Surabaya's climate",
                topNotes = listOf("Citrus", "Bergamot", "Fresh"),
                middleNotes = listOf("Floral", "Jasmine", "Rose"),
                baseNotes = listOf("Woody", "Musk", "Amber")
            )
        }
    }

    suspend fun discoverPerfumes(query: String): List<PersonaProfile> {
        try {
            val prompt = """
                Based on: "$query"
                
                Use Google Search to find 3-5 real perfumes that match this description.
                Include actual brand names, perfume names, and accurate note information.
                Focus on popular, available perfumes that fit the query.
                
                Respond with ONLY valid JSON array (no markdown, no code blocks):
                [
                    {
                        "brand": "Actual Brand",
                        "name": "Actual Perfume Name",
                        "analogy": "Vivid comparison",
                        "coreFeeling": "Feeling words",
                        "localContext": "Suitability for Surabaya (tropical, humid)",
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
            val perfumeList = perfumes.joinToString("\n") {
                "${it.id}. ${it.brand} ${it.name} - ${it.analogy} (${it.coreFeeling})"
            }

            val prompt = """
                Recommend ONE perfume from this list:
                
                $perfumeList
                
                Context:
                - Weather: ${weather.temperature}°C, ${weather.humidity}% humidity, ${weather.description}
                - Occasion: $occasion
                - Location: Surabaya, Indonesia
                
                Respond with ONLY valid JSON (no markdown, no code blocks):
                {
                    "perfumeId": <numeric id>,
                    "reason": "Brief explanation mentioning weather and occasion"
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = cleanJsonResponse(response.text ?: return null)
            val json = JSONObject(jsonText)

            val perfumeId = json.getInt("perfumeId")
            val reason = json.getString("reason")

            val selectedPerfume = perfumes.find { it.id == perfumeId }

            return selectedPerfume?.let { it to reason }
        } catch (e: Exception) {
            e.printStackTrace()
            return perfumes.firstOrNull()?.let {
                it to "A great choice for today!"
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
            val perfumeList = perfumes.joinToString("\n") {
                "${it.id}. ${it.brand} ${it.name} - ${it.analogy} (${it.coreFeeling}) [${it.localContext}]"
            }

            val prompt = """
                User request: "$userQuery"
                
                Available perfumes:
                $perfumeList
                
                Context:
                - Weather: ${weather.temperature}°C, ${weather.humidity}% humidity, ${weather.description}
                - Location: Surabaya, Indonesia (tropical)
                
                Recommend ONE perfume that best matches the request and weather.
                
                Respond with ONLY valid JSON (no markdown, no code blocks):
                {
                    "perfumeId": <numeric id>,
                    "reason": "2-3 sentences explaining the match"
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = cleanJsonResponse(response.text ?: return null)
            val json = JSONObject(jsonText)

            val perfumeId = json.getInt("perfumeId")
            val reason = json.getString("reason")

            val selectedPerfume = perfumes.find { it.id == perfumeId }

            return selectedPerfume?.let { it to reason }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}