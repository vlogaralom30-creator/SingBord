package com.example

enum class EmojiCategory(val icon: String, val title: String) {
    RECENT("🕒", "Recent"),
    SMILEYS("😀", "Smileys"),
    GESTURES("👍", "Gestures"),
    HEARTS("❤️", "Hearts"),
    ANIMALS("🐱", "Animals"),
    FOOD("🍔", "Food"),
    TRAVEL("🚗", "Travel"),
    OBJECTS("💡", "Objects"),
    FLAGS("🚩", "Flags")
}

object EmojiData {
    val smileys = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
        "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😋",
        "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔", "🤐",
        "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥", "😌",
        "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮", "🤧",
        "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "🥸", "😎", "🤓",
        "🧐", "😕", "😟", "🙁", "☹️", "😮", "😯", "😲", "😳", "🥺",
        "😦", "😧", "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😣",
        "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "😠", "🤬", "😈",
        "👿", "💀", "☠️", "💩", "🤡", "👻", "👽", "🤖"
    )

    val gestures = listOf(
        "👍", "👎", "👏", "🙌", "👐", "🤲", "🤝", "👊", "✊", "🤛",
        "🤜", "🤞", "✌️", "🤟", "🤘", "👌", "🤌", "🤏", "👈", "👉",
        "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "🤙", "💪",
        "🦾", "✍️", "🙏", "🦶", "🦵", "👂", "🦻", "👃", "🧠",
        "🫀", "🫁", "🦷", "🦴", "👀", "👁️", "👅", "👄", "💋"
    )

    val hearts = listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "💌",
        "💤", "💢", "💬", "👁️‍🗨️", "🗯️", "💭", "💮", "♨️", "✨", "⭐",
        "🌟", "💫", "💥", "🔥", "💯", "🎉", "🎊"
    )

    val animals = listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
        "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆",
        "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋",
        "🐌", "🐞", "🐜", "🦟", "🦗", "🕷️", "🦂", "🐢", "🐍", "🦎",
        "🦖", "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬",
        "🐳", "🦈", "🐊", "🐅", "🐆", "🦓", "🦍", "🦧", "🐘", "🦛",
        "🌸", "🌺", "🌹", "🌷", "🌻", "🌼", "🌴", "🌲", "🌳", "🍀"
    )

    val food = listOf(
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
        "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🥑", "🥦",
        "🥬", "🥒", "🌶️", "🌽", "🥕", "🧄", "🧅", "🥔", "🍠", "🥐",
        "🥯", "🍞", "🥖", "🥨", "🧀", "🥚", "🍳", "🧈", "🥞", "🧇",
        "🥓", "🥩", "🍗", "🍖", "🌭", "🍔", "🍟", "🍕", "🥪", "🥙",
        "🌮", "🌯", "🥗", "🍲", "🍜", "🍝", "🍣", "🍱", "🥟", "🍤",
        "🍦", "🍧", "🍨", "🍩", "🍪", "🎂", "🍰", "🧁", "🥧", "🍫",
        "☕", "🍵", "🧃", "🥤", "🧋", "🍺", "🍻", "🥂", "🍷"
    )

    val travel = listOf(
        "🚗", "🚕", "🚙", "🚌", "🏎️", "🚓", "🚑", "🚒", "🏍️", "🚲",
        "🛴", "🚨", "✈️", "🚀", "⛵", "🚢", "🚂", "🚆", "🚇", "🚊",
        "🚁", "🛸", "🛵", "🚜", "🚚", "🚛", "⛽", "🚦", "🏖️", "🏝️",
        "⛺", "🏠", "🏡", "🏢", "🏣", "🏥", "🏦", "🏨", "🏰", "🕌"
    )

    val objects = listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🥏", "🎱",
        "🏓", "🏸", "🏒", "🏏", "⛳", "🏹", "🥊", "🛹", "🎯", "🎮",
        "🎲", "⌚", "📱", "💻", "⌨️", "🖥️", "📷", "📸", "🎥", "📞",
        "☎️", "📺", "📻", "🎙️", "⏱️", "⏰", "💡", "🔦", "🕯️", "💸",
        "💵", "💰", "💳", "💎", "⚙️", "🔧", "🔨", "🔑", "🔒", "🔓",
        "🔔", "📦", "✉️", "📌"
    )

    val flags = listOf(
        "🇧🇩", "🏁", "🚩", "🎌", "🏴", "🏳️", "🇺🇸", "🇬🇧", "🇨🇦", "🇦🇺",
        "🇮🇳", "🇵🇰", "🇸🇦", "🇦🇪", "🇹🇷", "🇩🇪", "🇫🇷", "🇮🇹", "🇪🇸", "🇯🇵"
    )

    private val emojiKeywords: Map<String, List<String>> = mapOf(
        "smile" to listOf("😀", "😃", "😄", "😁", "😆", "😅", "😊", "🙂"),
        "happy" to listOf("😀", "😃", "😄", "😁", "😆", "🥳", "😊", "🥰"),
        "hashi" to listOf("😀", "😃", "😄", "😁", "😆", "🤣", "😂"),
        "khushi" to listOf("🥳", "😁", "🎉", "🥰", "😊"),
        "laugh" to listOf("🤣", "😂", "😆", "😹"),
        "love" to listOf("❤️", "💖", "💕", "😍", "🥰", "😘", "💗", "💓"),
        "bhalobasha" to listOf("❤️", "💖", "😍", "🥰", "😘", "💕"),
        "valobasha" to listOf("❤️", "💖", "😍", "🥰", "😘", "💕"),
        "prem" to listOf("❤️", "😍", "🥰", "💘", "💌"),
        "heart" to listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "💖"),
        "cry" to listOf("😭", "😢", "🥺", "😥", "😿"),
        "kanna" to listOf("😭", "😢", "🥺", "😥"),
        "sad" to listOf("😔", "😞", "😢", "😭", "🥺", "🙁", "☹️"),
        "dukkho" to listOf("😔", "😞", "😢", "😭", "💔"),
        "koshto" to listOf("😔", "😭", "💔", "🥺"),
        "fire" to listOf("🔥", "💥", "🥵", "🧨"),
        "agoon" to listOf("🔥", "💥"),
        "hot" to listOf("🔥", "🥵"),
        "cool" to listOf("😎", "🥶", "🕶️"),
        "chashma" to listOf("😎", "🤓", "🧐"),
        "kiss" to listOf("😘", "😗", "😚", "😙", "💋"),
        "chuma" to listOf("😘", "😚", "💋"),
        "thumb" to listOf("👍", "👎"),
        "like" to listOf("👍", "👌", "❤️"),
        "good" to listOf("👍", "👌", "👏", "✨", "💯"),
        "bhalo" to listOf("👍", "👌", "😊", "✨"),
        "clap" to listOf("👏", "🙌"),
        "pray" to listOf("🙏", "🤲"),
        "dua" to listOf("🤲", "🙏", "🕌"),
        "salam" to listOf("🤲", "🙏", "🤝"),
        "party" to listOf("🥳", "🎉", "🎊", "🎂", "🎈"),
        "birthday" to listOf("🎂", "🎉", "🎁", "🥳"),
        "anondo" to listOf("🥳", "🎉", "🎊", "😁"),
        "money" to listOf("💰", "💵", "💸", "💳", "🤑", "💎"),
        "taka" to listOf("💰", "💵", "💸", "🤑"),
        "food" to listOf("🍔", "🍕", "🍟", "🥪", "🍲", "🍜", "🍦", "🍰"),
        "khabar" to listOf("🍔", "🍕", "🍗", "🍚", "🍲", "🍜"),
        "bhat" to listOf("🍚", "🍲", "🍛"),
        "cha" to listOf("☕", "🍵", "🧋"),
        "tea" to listOf("☕", "🍵"),
        "coffee" to listOf("☕", "🧋"),
        "cat" to listOf("🐱", "🐈", "😺", "😸"),
        "biral" to listOf("🐱", "🐈"),
        "dog" to listOf("🐶", "🐕", "🦮"),
        "kukur" to listOf("🐶", "🐕"),
        "car" to listOf("🚗", "🚙", "🚕", "🏎️"),
        "gari" to listOf("🚗", "🚙", "🚕", "🚌"),
        "star" to listOf("⭐", "🌟", "✨", "💫"),
        "tara" to listOf("⭐", "🌟", "✨"),
        "bangla" to listOf("🇧🇩", "❤️", "🐅"),
        "bangladesh" to listOf("🇧🇩", "🐅", "🌾"),
        "bd" to listOf("🇧🇩"),
        "flag" to listOf("🇧🇩", "🏁", "🚩", "🎌"),
        "potaka" to listOf("🇧🇩", "🚩", "🏁"),
        "angry" to listOf("😡", "😠", "🤬", "👿"),
        "rag" to listOf("😡", "😠", "🤬"),
        "sleep" to listOf("😴", "🥱", "😪", "💤"),
        "ghum" to listOf("😴", "💤", "🛌"),
        "flower" to listOf("🌸", "🌺", "🌹", "🌷", "🌻"),
        "phul" to listOf("🌸", "🌺", "🌹", "🌷"),
        "shukriya" to listOf("🤲", "🙏", "✨"),
        "sun" to listOf("☀️", "🌞", "🌅"),
        "moon" to listOf("🌙", "🌚", "🌝"),
        "chand" to listOf("🌙", "🌚"),
        "rain" to listOf("🌧️", "🌦️", "☔", "⚡"),
        "brishti" to listOf("🌧️", "🌦️", "☔"),
        "music" to listOf("🎵", "🎶", "🎧", "🎸"),
        "gaan" to listOf("🎵", "🎶", "🎧")
    )

    fun getEmojis(category: EmojiCategory, recents: List<String>): List<String> {
        return when (category) {
            EmojiCategory.RECENT -> if (recents.isNotEmpty()) recents else smileys.take(28)
            EmojiCategory.SMILEYS -> smileys
            EmojiCategory.GESTURES -> gestures
            EmojiCategory.HEARTS -> hearts
            EmojiCategory.ANIMALS -> animals
            EmojiCategory.FOOD -> food
            EmojiCategory.TRAVEL -> travel
            EmojiCategory.OBJECTS -> objects
            EmojiCategory.FLAGS -> flags
        }
    }

    /**
     * Search emojis by keyword across English and Banglish search terms.
     */
    fun searchEmojis(query: String): List<String> {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return emptyList()

        val results = linkedSetOf<String>()

        // 1. Check direct keyword match
        emojiKeywords[clean]?.let { results.addAll(it) }

        // 2. Partial prefix / substring match
        for ((kw, list) in emojiKeywords) {
            if (kw.startsWith(clean) || kw.contains(clean) || clean.contains(kw)) {
                results.addAll(list)
            }
        }

        // 3. Fallback: if query matches category name
        EmojiCategory.values().forEach { cat ->
            if (cat.name.lowercase().contains(clean) || cat.title.lowercase().contains(clean)) {
                results.addAll(getEmojis(cat, emptyList()))
            }
        }

        return results.toList()
    }
}
