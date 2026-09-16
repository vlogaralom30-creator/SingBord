package com.example

/**
 * High-performance Unicode font transformer for SingBord.
 * Maps standard ASCII characters (a-z, A-Z, 0-9) to standard Unicode glyphs
 * (Mathematical Script, Blackboard Bold, Circled, Slashed, etc.).
 * Operates with 0ms latency and 0 external dependencies.
 */
enum class StylishFontStyle(val id: String, val label: String, val preview: String) {
    NORMAL("normal", "Normal", "abc 123"),
    CURSIVE("cursive", "𝒞𝓊𝓇𝓈𝒾𝓋ℯ", "𝓆𝓌ℯ 123"),
    BOLD_SCRIPT("bold_script", "𝓑𝓸𝓵𝓭 𝓢𝓬𝓻𝓲𝓹𝓽", "𝓺𝔀𝓮 123"),
    DOUBLE_STRUCK("double_struck", "𝔻𝕠𝕦𝕓𝕝𝕖", "𝕢𝕨𝕖 𝟙𝟚𝟛"),
    SLASHED("slashed", "S̷l̷a̷s̷h̷", "q̷w̷e̷ 1̷2̷3̷"),
    CIRCLED("circled", "Ⓒⓘⓡⓒⓛⓔⓓ", "ⓐⓑⓒ ①②③"),
    SMALL_CAPS("small_caps", "ꜱᴍᴀʟʟ ᴄᴀᴘꜱ", "ᴀʙᴄ 123"),
    MONOSPACE("monospace", "𝙼𝚘𝚗𝚘𝚜𝚙𝚊𝚌𝚎", "𝚚𝚠𝚎 𝟷𝟸𝟹")
}

object StylishFontEngine {

    val ALL_STYLES = StylishFontStyle.entries.toList()

    fun getStyle(id: String): StylishFontStyle {
        return ALL_STYLES.find { it.id.equals(id, ignoreCase = true) } ?: StylishFontStyle.NORMAL
    }

    /**
     * Transforms a single character into the target Unicode style string.
     */
    fun transformChar(c: Char, style: StylishFontStyle): String {
        if (style == StylishFontStyle.NORMAL) return c.toString()

        return when (style) {
            StylishFontStyle.NORMAL -> c.toString()
            StylishFontStyle.CURSIVE -> toCursive(c)
            StylishFontStyle.BOLD_SCRIPT -> toBoldScript(c)
            StylishFontStyle.DOUBLE_STRUCK -> toDoubleStruck(c)
            StylishFontStyle.SLASHED -> toSlashed(c)
            StylishFontStyle.CIRCLED -> toCircled(c)
            StylishFontStyle.SMALL_CAPS -> toSmallCaps(c)
            StylishFontStyle.MONOSPACE -> toMonospace(c)
        }
    }

    /**
     * Transforms a word or string into the selected font style.
     */
    fun transformText(text: String, style: StylishFontStyle): String {
        if (style == StylishFontStyle.NORMAL || text.isEmpty()) return text

        val sb = java.lang.StringBuilder(text.length * 2)
        for (ch in text) {
            sb.append(transformChar(ch, style))
        }
        return sb.toString()
    }

    // 1. Cursive / Script: 𝒶 𝒷 𝒸 / 𝒜 ℬ 𝒞
    private fun toCursive(c: Char): String {
        return when (c) {
            'a' -> "𝒶"; 'b' -> "𝒷"; 'c' -> "𝒸"; 'd' -> "𝒹"; 'e' -> "ℯ"
            'f' -> "𝒻"; 'g' -> "ℊ"; 'h' -> "𝒽"; 'i' -> "𝒾"; 'j' -> "𝒿"
            'k' -> "𝓀"; 'l' -> "𝓁"; 'm' -> "𝓂"; 'n' -> "𝓃"; 'o' -> "ℴ"
            'p' -> "𝓅"; 'q' -> "𝓆"; 'r' -> "𝓇"; 's' -> "𝓈"; 't' -> "𝓉"
            'u' -> "𝓊"; 'v' -> "𝓋"; 'w' -> "𝓌"; 'x' -> "𝓍"; 'y' -> "𝓎"; 'z' -> "𝓏"
            'A' -> "𝒜"; 'B' -> "ℬ"; 'C' -> "𝒞"; 'D' -> "𝒟"; 'E' -> "ℰ"
            'F' -> "ℱ"; 'G' -> "𝒢"; 'H' -> "ℋ"; 'I' -> "ℐ"; 'J' -> "𝒥"
            'K' -> "𝒦"; 'L' -> "ℒ"; 'M' -> "ℳ"; 'N' -> "𝒩"; 'O' -> "𝒪"
            'P' -> "𝒫"; 'Q' -> "𝒬"; 'R' -> "ℛ"; 'S' -> "𝒮"; 'T' -> "𝒯"
            'U' -> "𝒰"; 'V' -> "𝒱"; 'W' -> "𝒲"; 'X' -> "𝒳"; 'Y' -> "𝒴"; 'Z' -> "𝒵"
            else -> c.toString()
        }
    }

    // 2. Bold Script: 𝓪 𝓫 𝓬 / 𝓐 𝓑 𝓒
    private fun toBoldScript(c: Char): String {
        return when (c) {
            'a' -> "𝓪"; 'b' -> "𝓫"; 'c' -> "𝓬"; 'd' -> "𝓭"; 'e' -> "𝓮"
            'f' -> "𝓯"; 'g' -> "𝓰"; 'h' -> "𝓱"; 'i' -> "𝓲"; 'j' -> "𝓳"
            'k' -> "𝓴"; 'l' -> "𝓵"; 'm' -> "𝓶"; 'n' -> "𝓷"; 'o' -> "𝓸"
            'p' -> "𝓹"; 'q' -> "𝓺"; 'r' -> "𝓻"; 's' -> "𝓼"; 't' -> "𝓽"
            'u' -> "𝓾"; 'v' -> "𝓿"; 'w' -> "𝔀"; 'x' -> "𝔁"; 'y' -> "𝔂"; 'z' -> "𝔃"
            'A' -> "𝓐"; 'B' -> "𝓑"; 'C' -> "𝓒"; 'D' -> "𝓓"; 'E' -> "𝓔"
            'F' -> "𝓕"; 'G' -> "𝓖"; 'H' -> "𝓗"; 'I' -> "𝓘"; 'J' -> "𝓙"
            'K' -> "𝓚"; 'L' -> "𝓛"; 'M' -> "𝓜"; 'N' -> "𝓝"; 'O' -> "𝓞"
            'P' -> "𝓟"; 'Q' -> "𝓠"; 'R' -> "𝓡"; 'S' -> "𝓢"; 'T' -> "𝓣"
            'U' -> "𝓤"; 'V' -> "𝓥"; 'W' -> "𝓦"; 'X' -> "𝓧"; 'Y' -> "𝓨"; 'Z' -> "𝓩"
            else -> c.toString()
        }
    }

    // 3. Double-Struck: 𝕒 𝕓 𝕔 / 𝔸 𝔹 ℂ / 𝟘 𝟙 𝟚
    private fun toDoubleStruck(c: Char): String {
        return when (c) {
            'a' -> "𝕒"; 'b' -> "𝕓"; 'c' -> "𝕔"; 'd' -> "𝕕"; 'e' -> "𝕖"
            'f' -> "𝕗"; 'g' -> "𝕘"; 'h' -> "𝕙"; 'i' -> "𝕚"; 'j' -> "𝕛"
            'k' -> "𝕜"; 'l' -> "𝕝"; 'm' -> "𝕞"; 'n' -> "𝕟"; 'o' -> "𝕠"
            'p' -> "𝕡"; 'q' -> "𝕢"; 'r' -> "𝕣"; 's' -> "𝕤"; 't' -> "𝕥"
            'u' -> "𝕦"; 'v' -> "𝕧"; 'w' -> "𝕨"; 'x' -> "𝕩"; 'y' -> "𝕪"; 'z' -> "𝕫"
            'A' -> "𝔸"; 'B' -> "𝔹"; 'C' -> "ℂ"; 'D' -> "𝔻"; 'E' -> "𝔼"
            'F' -> "𝔽"; 'G' -> "𝔾"; 'H' -> "ℍ"; 'I' -> "𝕀"; 'J' -> "𝕁"
            'K' -> "𝕂"; 'L' -> "𝕃"; 'M' -> "𝕄"; 'N' -> "ℕ"; 'O' -> "𝕆"
            'P' -> "ℙ"; 'Q' -> "ℚ"; 'R' -> "ℝ"; 'S' -> "𝕊"; 'T' -> "𝕋"
            'U' -> "𝕌"; 'V' -> "𝕍"; 'W' -> "𝕎"; 'X' -> "𝕏"; 'Y' -> "𝕐"; 'Z' -> "ℤ"
            '0' -> "𝟘"; '1' -> "𝟙"; '2' -> "𝟚"; '3' -> "𝟛"; '4' -> "𝟜"
            '5' -> "𝟝"; '6' -> "𝟞"; '7' -> "𝟟"; '8' -> "𝟠"; '9' -> "𝟡"
            else -> c.toString()
        }
    }

    // 4. Slashed: q̷ w̷ e̷ r̷ t̷ / 1̷ 2̷ 3̷ (Using combining long solidus overlay U+0338)
    private fun toSlashed(c: Char): String {
        if (c.isWhitespace()) return c.toString()
        return "$c\u0338"
    }

    // 5. Circled: ⓐ ⓑ ⓒ / Ⓐ Ⓑ Ⓒ / ① ② ③
    private fun toCircled(c: Char): String {
        return when (c) {
            'a' -> "ⓐ"; 'b' -> "ⓑ"; 'c' -> "ⓒ"; 'd' -> "ⓓ"; 'e' -> "ⓔ"
            'f' -> "ⓕ"; 'g' -> "ⓖ"; 'h' -> "ⓗ"; 'i' -> "ⓘ"; 'j' -> "ⓙ"
            'k' -> "ⓚ"; 'l' -> "ⓛ"; 'm' -> "ⓜ"; 'n' -> "ⓝ"; 'o' -> "ⓞ"
            'p' -> "ⓟ"; 'q' -> "ⓠ"; 'r' -> "ⓡ"; 's' -> "ⓢ"; 't' -> "ⓣ"
            'u' -> "ⓤ"; 'v' -> "ⓥ"; 'w' -> "ⓦ"; 'x' -> "ⓧ"; 'y' -> "ⓨ"; 'z' -> "ⓩ"
            'A' -> "Ⓐ"; 'B' -> "Ⓑ"; 'C' -> "Ⓒ"; 'D' -> "Ⓓ"; 'E' -> "Ⓔ"
            'F' -> "Ⓕ"; 'G' -> "Ⓖ"; 'H' -> "Ⓗ"; 'I' -> "Ⓘ"; 'J' -> "Ⓙ"
            'K' -> "Ⓚ"; 'L' -> "Ⓛ"; 'M' -> "Ⓜ"; 'N' -> "Ⓝ"; 'O' -> "Ⓞ"
            'P' -> "Ⓟ"; 'Q' -> "Ⓠ"; 'R' -> "Ⓡ"; 'S' -> "Ⓢ"; 'T' -> "Ⓣ"
            'U' -> "Ⓤ"; 'V' -> "Ⓥ"; 'W' -> "Ⓦ"; 'X' -> "Ⓧ"; 'Y' -> "Ⓨ"; 'Z' -> "Ⓩ"
            '0' -> "⓪"; '1' -> "①"; '2' -> "②"; '3' -> "③"; '4' -> "④"
            '5' -> "⑤"; '6' -> "⑥"; '7' -> "⑦"; '8' -> "⑧"; '9' -> "⑨"
            else -> c.toString()
        }
    }

    // 6. Small Caps: ᴀ ʙ ᴄ ᴅ ᴇ ꜰ ɢ
    private fun toSmallCaps(c: Char): String {
        return when (c.lowercaseChar()) {
            'a' -> "ᴀ"; 'b' -> "ʙ"; 'c' -> "ᴄ"; 'd' -> "ᴅ"; 'e' -> "ᴇ"
            'f' -> "ꜰ"; 'g' -> "ɢ"; 'h' -> "ʜ"; 'i' -> "ɪ"; 'j' -> "ᴊ"
            'k' -> "ᴋ"; 'l' -> "ʟ"; 'm' -> "ᴍ"; 'n' -> "ɴ"; 'o' -> "ᴏ"
            'p' -> "ᴘ"; 'q' -> "ǫ"; 'r' -> "ʀ"; 's' -> "ꜱ"; 't' -> "ᴛ"
            'u' -> "ᴜ"; 'v' -> "ᴠ"; 'w' -> "ᴡ"; 'x' -> "x"; 'y' -> "ʏ"; 'z' -> "ᴢ"
            else -> c.toString()
        }
    }

    // 7. Monospace: 𝚚 𝚠 𝚎 𝚛 / 𝟷 𝟸 𝟹
    private fun toMonospace(c: Char): String {
        return when (c) {
            'a' -> "𝚊"; 'b' -> "𝚋"; 'c' -> "𝚌"; 'd' -> "𝚍"; 'e' -> "𝚎"
            'f' -> "𝚏"; 'g' -> "𝚐"; 'h' -> "𝚑"; 'i' -> "𝚒"; 'j' -> "𝚓"
            'k' -> "𝚔"; 'l' -> "𝚕"; 'm' -> "𝚖"; 'n' -> "𝚗"; 'o' -> "𝚘"
            'p' -> "𝚙"; 'q' -> "𝚚"; 'r' -> "𝚛"; 's' -> "𝚜"; 't' -> "𝚝"
            'u' -> "𝚞"; 'v' -> "𝚟"; 'w' -> "𝚠"; 'x' -> "𝚡"; 'y' -> "𝚢"; 'z' -> "𝚣"
            'A' -> "𝙰"; 'B' -> "𝙱"; 'C' -> "𝙲"; 'D' -> "𝙳"; 'E' -> "𝙴"
            'F' -> "𝙵"; 'G' -> "𝙶"; 'H' -> "𝙷"; 'I' -> "𝙸"; 'J' -> "𝙹"
            'K' -> "𝙺"; 'L' -> "𝙻"; 'M' -> "𝙼"; 'N' -> "𝙽"; 'O' -> "𝙾"
            'P' -> "𝙿"; 'Q' -> "𝚀"; 'R' -> "𝚁"; 'S' -> "𝚂"; 'T' -> "𝚃"
            'U' -> "𝚄"; 'V' -> "𝚅"; 'W' -> "𝚆"; 'X' -> "𝚇"; 'Y' -> "𝚈"; 'Z' -> "𝚉"
            '0' -> "𝟶"; '1' -> "𝟷"; '2' -> "𝟸"; '3' -> "𝟹"; '4' -> "𝟺"
            '5' -> "𝟻"; '6' -> "𝟼"; '7' -> "𝟽"; '8' -> "𝟾"; '9' -> "𝟿"
            else -> c.toString()
        }
    }
}
