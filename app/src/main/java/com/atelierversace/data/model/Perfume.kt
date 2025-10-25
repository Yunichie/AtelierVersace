package com.atelierversace.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfumes")
data class Perfume(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val brand: String,
    val name: String,
    val imageUri: String,
    val analogy: String,
    val coreFeeling: String,
    val localContext: String,
    val topNotes: String = "",
    val middleNotes: String = "",
    val baseNotes: String = "",
    val isWishlist: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
