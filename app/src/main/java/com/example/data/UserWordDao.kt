package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserWordDao {
    @Query("SELECT * FROM user_words ORDER BY frequency DESC, lastUsed DESC")
    fun getAllWordsFlow(): Flow<List<UserWord>>

    @Query("SELECT * FROM user_words ORDER BY frequency DESC, lastUsed DESC")
    suspend fun getAllWordsSync(): List<UserWord>

    @Query("SELECT * FROM user_words ORDER BY frequency DESC, lastUsed DESC LIMIT 200")
    suspend fun getTopWords(): List<UserWord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(word: UserWord)

    @Query("SELECT * FROM user_words WHERE word = :w LIMIT 1")
    suspend fun getWord(w: String): UserWord?

    @Transaction
    suspend fun recordWordUsage(rawWord: String) {
        val clean = rawWord.trim().lowercase()
        // Allow valid Latin letters, apostrophes, and any Bengali Unicode character (consonants, vowel marks, virama)
        val isValid = clean.length >= 2 && clean.all { it.isLetter() || it in '\u0980'..'\u09FF' || it == '\'' }
        if (!isValid) return

        val existing = getWord(clean)
        if (existing != null) {
            insertOrUpdate(existing.copy(frequency = existing.frequency + 1, lastUsed = System.currentTimeMillis()))
        } else {
            insertOrUpdate(UserWord(word = clean, frequency = 1, lastUsed = System.currentTimeMillis()))
        }
    }

    @Query("DELETE FROM user_words WHERE word = :w")
    suspend fun deleteWord(w: String)

    @Query("DELETE FROM user_words")
    suspend fun clearAll()
}
