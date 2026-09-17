package com.example.data

import androidx.room.Entity

/**
 * Entity representing bigram transitions (word-to-word sequence).
 * Used for predictive next-word suggestions based on user typing habits.
 * e.g., "ami" -> "tomake", "jani na", "tomay"
 */
@Entity(
    tableName = "user_bigrams",
    primaryKeys = ["prevWord", "nextWord"]
)
data class UserWordBigram(
    val prevWord: String,
    val nextWord: String,
    val frequency: Int = 1,
    val lastUsed: Long = System.currentTimeMillis()
)
