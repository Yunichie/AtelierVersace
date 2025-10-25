package com.atelierversace.data.model

data class PersonaProfile(
    val brand: String,
    val name: String,
    val analogy: String,
    val coreFeeling: String,
    val localContext: String,
    val topNotes: List<String> = emptyList(),
    val middleNotes: List<String> = emptyList(),
    val baseNotes: List<String> = emptyList()
)