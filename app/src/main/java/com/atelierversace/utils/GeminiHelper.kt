package com.atelierversace.utils

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.atelierversace.data.model.PersonaProfile
import com.atelierversace.data.model.Perfume
import org.json.JSONObject
import org.json.JSONArray
import com.atelierversace.BuildConfig

class GeminiHelper {

    private val visionModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_KEY
    )

    private val textModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_KEY
    )

    suspend fun identifyPerfume(bitmap: Bitmap): Pair<String, String>? {
        try {
            val prompt = """
                Identify this perfume from the image. Look at the bottle, label, and any text visible.
                Respond ONLY with a JSON object in this exact format WITHOUT codeblock:
                {"brand": "Brand Name", "name": "Perfume Name"}
                
                If you cannot identify the perfume, respond with WITHOUT codeblock:
                {"brand": "Unknown", "name": "Perfume"}
            """.trimIndent()

            val response = visionModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            val jsonText = response.text?.trim() ?: return null
            val json = JSONObject(jsonText)

            return Pair(
                json.getString("brand"),
                json.getString("name")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun generatePersonaProfile(brand: String, name: String): PersonaProfile {
        try {
            val prompt = """
                Create a persona profile for the perfume: $brand $name
                
                Consider the context: Surabaya, Indonesia (tropical, humid climate).
                
                Respond ONLY with a JSON object in this exact format WITHOUT codeblock:
                {
                    "brand": "Brand Name",
                    "name": "Perfume Name",
                    "analogy": "A simple, evocative analogy (e.g., 'Smells like a cozy cafe on a rainy day')",
                    "coreFeeling": "One or two words (e.g., 'Comforting & Warm')",
                    "localContext": "Brief suitability for Surabaya (e.g., 'Best for evenings or air-conditioned rooms')",
                    "topNotes": ["Note1", "Note2", "Note3"],
                    "middleNotes": ["Note1", "Note2", "Note3"],
                    "baseNotes": ["Note1", "Note2", "Note3"]
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = response.text?.trim() ?: throw Exception("Empty response")

            val json = JSONObject(jsonText)

            val topNotesArray = json.getJSONArray("topNotes")
            val middleNotesArray = json.getJSONArray("middleNotes")
            val baseNotesArray = json.getJSONArray("baseNotes")

            return PersonaProfile(
                brand = json.getString("brand"),
                name = json.getString("name"),
                analogy = json.getString("analogy"),
                coreFeeling = json.getString("coreFeeling"),
                localContext = json.getString("localContext"),
                topNotes = (0 until topNotesArray.length()).map { topNotesArray.getString(it) },
                middleNotes = (0 until middleNotesArray.length()).map { middleNotesArray.getString(it) },
                baseNotes = (0 until baseNotesArray.length()).map { baseNotesArray.getString(it) }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return PersonaProfile(
                brand = brand,
                name = name,
                analogy = "A mysterious and elegant scent",
                coreFeeling = "Sophisticated",
                localContext = "Versatile for various occasions",
                topNotes = listOf("Fresh", "Citrus", "Bergamot"),
                middleNotes = listOf("Floral", "Jasmine", "Rose"),
                baseNotes = listOf("Woody", "Amber", "Musk")
            )
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
                Recommend ONE perfume from this list for the user:
                
                $perfumeList
                
                Context:
                - Weather: ${weather.temperature}°C, ${weather.humidity}% humidity, ${weather.description}
                - Occasion: $occasion
                - Location: Surabaya, Indonesia
                
                Respond ONLY with a JSON object WITHOUT codeblock:
                {
                    "perfumeId": <the id number>,
                    "reason": "One sentence explaining why (mention weather/occasion)"
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = response.text?.trim() ?: return null
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
                Recommend ONE perfume from this list based on the user's request:
                
                User's Request: "$userQuery"
                
                Available Perfumes:
                $perfumeList
                
                Current Context:
                - Weather: ${weather.temperature}°C, ${weather.humidity}% humidity, ${weather.description}
                - Location: Surabaya, Indonesia (tropical, humid climate)
                
                Consider:
                1. User's mood/occasion from their query
                2. Current weather conditions
                3. Perfume characteristics and suitability
                
                Respond ONLY with a JSON object WITHOUT codeblock:
                {
                    "perfumeId": <the id number>,
                    "reason": "2-3 sentences explaining why this perfume matches their request and current weather"
                }
            """.trimIndent()

            val response = textModel.generateContent(prompt)
            val jsonText = response.text?.trim() ?: return null
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

    suspend fun discoverPerfumes(query: String): List<PersonaProfile> {
        try {
            val prompt = """
                Based on this query: "$query"
                
                Suggest 3-5 perfumes that match this feeling/description.
                Include the actual brand name and perfume name for each recommendation.
                Prioritize trendy perfumes for the query if possible.
                Consider real perfumes that exist in the market.
                
                Respond ONLY with a JSON array WITHOUT codeblock:
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
            val profiles = mutableListOf<PersonaProfile>()

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                val topNotesArray = json.getJSONArray("topNotes")
                val middleNotesArray = json.getJSONArray("middleNotes")
                val baseNotesArray = json.getJSONArray("baseNotes")

                profiles.add(
                    PersonaProfile(
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
}