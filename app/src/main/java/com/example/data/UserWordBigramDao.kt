package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserWordBigramDao {

    @Query("SELECT * FROM user_bigrams WHERE prevWord = :prev ORDER BY frequency DESC, lastUsed DESC LIMIT :limit")
    suspend fun getNextWordsFor(prev: String, limit: Int = 10): List<UserWordBigram>

    @Query("SELECT * FROM user_bigrams ORDER BY frequency DESC, lastUsed DESC LIMIT 500")
    suspend fun getTopBigrams(): List<UserWordBigram>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(bigram: UserWordBigram)

    @Query("SELECT * FROM user_bigrams WHERE prevWord = :prev AND nextWord = :next LIMIT 1")
    suspend fun getBigram(prev: String, next: String): UserWordBigram?

    @Transaction
    suspend fun recordTransition(prev: String, next: String) {
        val cleanPrev = prev.trim().lowercase()
        val cleanNext = next.trim().lowercase()
        if (cleanPrev.isBlank() || cleanNext.isBlank()) return

        val existing = getBigram(cleanPrev, cleanNext)
        if (existing != null) {
            insertOrUpdate(existing.copy(frequency = existing.frequency + 1, lastUsed = System.currentTimeMillis()))
        } else {
            insertOrUpdate(UserWordBigram(prevWord = cleanPrev, nextWord = cleanNext, frequency = 1, lastUsed = System.currentTimeMillis()))
        }
    }

    @Query("DELETE FROM user_bigrams WHERE prevWord = :prev")
    suspend fun deleteBigramsFor(prev: String)

    @Query("DELETE FROM user_bigrams")
    suspend fun clearAll()
}
