package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testBanglishSuggestions() {
    val suggestions = WordSuggestionEngine.getSuggestions("kem", enableBanglish = true)
    assertTrue("Should suggest kemon for kem", suggestions.any { it.equals("kemon", ignoreCase = true) })
  }

  @Test
  fun testCasePreservation() {
    val upperSuggestions = WordSuggestionEngine.getSuggestions("KEM", enableBanglish = true)
    assertTrue("Should be uppercase", upperSuggestions.any { it == "KEMON" })

    val capitalizedSuggestions = WordSuggestionEngine.getSuggestions("Ami", enableBanglish = true)
    assertTrue("Should be capitalized", capitalizedSuggestions.any { it == "Ami" })
  }
}
