package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a word memorized by SingBord keyboard.
 * Tracks typing frequency and last used timestamp for intelligent ranking.
 */
@Entity(tableName = "user_words")
data class UserWord(
    @PrimaryKey
    val word: String,
    val frequency: Int = 1,
    val lastUsed: Long = System.currentTimeMillis()
)
