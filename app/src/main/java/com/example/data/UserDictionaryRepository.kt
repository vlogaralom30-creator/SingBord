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
    private val bigramDao = db.userWordBigramDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Ultra-fast in-memory frequency cache for zero-latency suggestions on UI thread
    private val memoryFrequencyCache = ConcurrentHashMap<String, Int>()

    // In-memory bigram transitions cache: prevWord -> (nextWord -> frequency)
    private val memoryBigramCache = ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>()

    init {
        // Preload learned words and bigrams into memory cache
        scope.launch {
            try {
                val words = dao.getTopWords()
                words.forEach {
                    memoryFrequencyCache[it.word.lowercase()] = it.frequency
                }
            } catch (e: Exception) {
                // Fallback gracefully
            }

            try {
                val bigrams = bigramDao.getTopBigrams()
                bigrams.forEach {
                    val map = memoryBigramCache.getOrPut(it.prevWord.lowercase()) { ConcurrentHashMap() }
                    map[it.nextWord.lowercase()] = it.frequency
                }
            } catch (e: Exception) {
                // Fallback gracefully
            }
        }
    }

    private fun isValidWord(clean: String): Boolean {
        return clean.length >= 2 && clean.all { it.isLetter() || it in '\u0980'..'\u09FF' || it == '\'' }
    }

    /**
     * Records a learned word asynchronously and increments in-memory cache immediately.
     */
    fun recordWord(rawWord: String) {
        val clean = rawWord.trim().lowercase()
        if (!isValidWord(clean)) return

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
     * Records bigram sequence (prevWord -> nextWord) for context-aware next-word prediction.
     */
    fun recordBigram(prev: String, next: String) {
        val cleanPrev = prev.trim().lowercase()
        val cleanNext = next.trim().lowercase()
        if (!isValidWord(cleanPrev) || !isValidWord(cleanNext) || cleanPrev == cleanNext) return

        // Instant update in memory
        val nextMap = memoryBigramCache.getOrPut(cleanPrev) { ConcurrentHashMap() }
        val count = nextMap[cleanNext] ?: 0
        nextMap[cleanNext] = count + 1

        // Background persistence
        scope.launch {
            try {
                bigramDao.recordTransition(cleanPrev, cleanNext)
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    /**
     * Retrieves predictions for the next word following prevWord, ranked by frequency.
     */
    fun getPredictedNextWords(prevWord: String, limit: Int = 5): List<String> {
        val cleanPrev = prevWord.trim().lowercase()
        val nextMap = memoryBigramCache[cleanPrev] ?: return emptyList()
        return nextMap.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }

    /**
     * Manually adds or updates a custom word with specific frequency.
     */
    fun addCustomWord(rawWord: String, frequency: Int = 5) {
        val clean = rawWord.trim().lowercase()
        if (!isValidWord(clean)) return

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
        memoryBigramCache.remove(clean)
        scope.launch {
            try {
                dao.deleteWord(clean)
                bigramDao.deleteBigramsFor(clean)
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
        memoryBigramCache.clear()
        scope.launch {
            try {
                dao.clearAll()
                bigramDao.clearAll()
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
