package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository for managing user dictionary with instant in-memory lookup (<1ms)
 * backed by persistent Room database.
 */
class UserDictionaryRepository private constructor(context: Context) {

    private val db = SingBordDatabase.getInstance(context)
    private val dao = db.userWordDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Ultra-fast in-memory frequency cache for zero-latency suggestions on UI thread
    private val memoryFrequencyCache = ConcurrentHashMap<String, Int>()

    init {
        // Preload learned words into memory cache
        scope.launch {
            try {
                val words = dao.getTopWords()
                words.forEach {
                    memoryFrequencyCache[it.word.lowercase()] = it.frequency
                }
            } catch (e: Exception) {
                // Fallback gracefully
            }
        }
    }

    /**
     * Records a learned word asynchronously and increments in-memory cache immediately.
     */
    fun recordWord(rawWord: String) {
        val clean = rawWord.trim().lowercase()
        if (clean.length < 2 || !clean.all { it.isLetter() }) return

        // Update in-memory frequency immediately for instant ranking in next keystrokes
        val currentCount = memoryFrequencyCache[clean] ?: 0
        memoryFrequencyCache[clean] = currentCount + 1

        // Persist to Room database in background thread
        scope.launch {
            try {
                dao.recordWordUsage(clean)
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    /**
     * Manually adds or updates a custom word with specific frequency.
     */
    fun addCustomWord(rawWord: String, frequency: Int = 5) {
        val clean = rawWord.trim().lowercase()
        if (clean.length < 2 || !clean.all { it.isLetter() }) return

        memoryFrequencyCache[clean] = frequency
        scope.launch {
            try {
                dao.insertOrUpdate(UserWord(word = clean, frequency = frequency, lastUsed = System.currentTimeMillis()))
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    /**
     * Deletes a word from cache and database.
     */
    fun deleteWord(rawWord: String) {
        val clean = rawWord.trim().lowercase()
        memoryFrequencyCache.remove(clean)
        scope.launch {
            try {
                dao.deleteWord(clean)
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    /**
     * Clears all learned words.
     */
    fun clearAll() {
        memoryFrequencyCache.clear()
        scope.launch {
            try {
                dao.clearAll()
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    /**
     * Returns learned word frequency from in-memory cache.
     */
    fun getFrequency(word: String): Int {
        return memoryFrequencyCache[word.lowercase()] ?: 0
    }

    /**
     * Snapshot of all learned words in memory.
     */
    fun getMemoryLearnedWords(): Map<String, Int> {
        return memoryFrequencyCache
    }

    /**
     * Observes all learned words as Flow for UI in MainActivity.
     */
    fun getAllWordsFlow(): Flow<List<UserWord>> = dao.getAllWordsFlow()

    companion object {
        @Volatile
        private var INSTANCE: UserDictionaryRepository? = null

        fun getInstance(context: Context): UserDictionaryRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserDictionaryRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
