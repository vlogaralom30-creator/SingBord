package com.example

/**
 * Phonetic Transliteration Engine for Avro Bangla typing mode.
 * Converts Romanized phonetic input into standard Bangla Unicode.
 */
object AvroPhoneticEngine {

    private val directPatterns = listOf(
        // Vowels standalone
        "aa" to "আ",
        "oi" to "ঐ",
        "ou" to "ঔ",
        "ii" to "ঈ",
        "uu" to "ঊ",
        "a" to "অ",
        "o" to "ও",
        "i" to "ই",
        "u" to "উ",
        "e" to "এ",

        // Consonant clusters / Juktakkhor
        "kkh" to "ক্ষ",
        "kkhn" to "ক্ষ্ণ",
        "ggy" to "জ্ঞ",
        "ngk" to "ঙ্ক",
        "ngkh" to "ঙ্খ",
        "ngg" to "ঙ্গ",
        "nggh" to "ঙ্ঘ",
        "cch" to "চ্ছ",
        "jjh" to "জ্ঝ",
        "nch" to "ঞ্চ",
        "njh" to "ঞ্জ",
        "ntth" to "ণ্ঠ",
        "nddh" to "ণ্ঢ",
        "tth" to "ত্থ",
        "ddh" to "দ্ধ",
        "nt" to "ন্ত",
        "nth" to "ন্থ",
        "nd" to "ন্দ",
        "ndh" to "ন্ধ",
        "mph" to "ম্ফ",
        "mbh" to "ম্ভ",
        "shch" to "শ্চ",
        "shth" to "ষ্ঠ",
        "shthh" to "ষ্ফ",
        "shk" to "ষ্ক",
        "shkh" to "ষ্খ",
        "sht" to "ষ্ট",
        "shph" to "স্ফ",
        "sk" to "স্ক",
        "skh" to "স্খ",
        "st" to "স্ত",
        "sth" to "স্থ",
        "sph" to "স্ফ",
        "hm" to "হ্ম",
        "hn" to "হ্ন",
        "hl" to "হ্ল",

        // Aspirated & specialized consonants
        "kh" to "খ",
        "gh" to "ঘ",
        "ng" to "ঙ",
        "ch" to "চ",
        "chh" to "ছ",
        "jh" to "ঝ",
        "Th" to "ঠ",
        "Dh" to "ঢ",
        "th" to "থ",
        "dh" to "ধ",
        "ph" to "ফ",
        "bh" to "ভ",
        "sh" to "শ",
        "Sh" to "ষ",
        "Rh" to "ঢ়",
        "rr" to "ড়",

        // Single consonants
        "k" to "ক",
        "g" to "গ",
        "c" to "চ",
        "j" to "জ",
        "T" to "ট",
        "D" to "ড",
        "N" to "ণ",
        "t" to "ত",
        "d" to "দ",
        "n" to "ন",
        "p" to "প",
        "f" to "ফ",
        "b" to "ব",
        "v" to "ভ",
        "m" to "ম",
        "z" to "য",
        "y" to "য়",
        "r" to "র",
        "l" to "ল",
        "s" to "স",
        "h" to "হ",
        "R" to "ড়"
    )

    private val karMap = mapOf(
        "aa" to "া",
        "a" to "া",
        "i" to "ি",
        "ee" to "ী",
        "ii" to "ী",
        "u" to "ু",
        "oo" to "ূ",
        "uu" to "ূ",
        "e" to "ে",
        "oi" to "ৈ",
        "o" to "ো",
        "ou" to "ৌ"
    )

    /**
     * Translates a phonetic romanized word into Bengali Unicode.
     */
    fun parse(input: String): String {
        if (input.isBlank()) return input

        // Quick word dictionary for high accuracy common words
        val directWord = commonPhoneticWords[input.lowercase()]
        if (directWord != null) return directWord

        val sb = StringBuilder()
        var i = 0
        val len = input.length
        var lastWasConsonant = false

        while (i < len) {
            var matched = false

            // Try 3-char, 2-char, then 1-char combinations
            for (matchLen in 4 downTo 1) {
                if (i + matchLen <= len) {
                    val sub = input.substring(i, i + matchLen)

                    // If previous token was consonant and current matches a vowel kar
                    if (lastWasConsonant && karMap.containsKey(sub)) {
                        sb.append(karMap[sub])
                        i += matchLen
                        matched = true
                        lastWasConsonant = false
                        break
                    }

                    // Otherwise check direct patterns
                    val target = directPatterns.firstOrNull { it.first == sub }
                    if (target != null) {
                        sb.append(target.second)
                        i += matchLen
                        matched = true
                        lastWasConsonant = isConsonantBangla(target.second)
                        break
                    }
                }
            }

            if (!matched) {
                sb.append(input[i])
                lastWasConsonant = false
                i++
            }
        }

        return sb.toString()
    }

    private fun isConsonantBangla(str: String): Boolean {
        if (str.isEmpty()) return false
        val c = str.last()
        return c in '\u0995'..'\u09B9' || c == 'ড়' || c == 'ঢ়' || c == 'য়'
    }

    private val commonPhoneticWords = mapOf(
        "ami" to "আমি",
        "tumi" to "তুমি",
        "apni" to "আপনি",
        "kemon" to "কেমন",
        "achho" to "আছো",
        "achen" to "আছেন",
        "achi" to "আছি",
        "valo" to "ভালো",
        "bhalo" to "ভালো",
        "ki" to "কি",
        "khobor" to "খবর",
        "dhonnobad" to "ধন্যবাদ",
        "bangla" to "বাংলা",
        "bangladesh" to "বাংলাদেশ",
        "shundor" to "সুন্দর",
        "kokhon" to "কখন",
        "kothay" to "কোথায়",
        "keno" to "কেন",
        "ha" to "হ্যাঁ",
        "na" to "না",
        "onek" to "অনেক",
        "shob" to "সব",
        "bondhu" to "বন্ধু",
        "kaj" to "কাজ",
        "shomoy" to "সময়",
        "din" to "দিন",
        "rat" to "রাত",
        "dekha" to "দেখা",
        "hobe" to "হবে"
    )
}
