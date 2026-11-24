package com.example.util

import android.content.SharedPreferences

/**
 * Hidden canine Easter eggs discoverable throughout BarkTalk.
 */
enum class BarkEasterEgg(
    val id: String,
    val title: String,
    val emoji: String,
    val badgeLabel: String,
    val secretMessage: String,
    val crypticHint: String
) {
    SNOOT_BOOP_ZOOMIES(
        id = "snoot_boop_zoomies",
        title = "Turbo Zoomies Mode!",
        emoji = "⚡🐾",
        badgeLabel = "Certified Snoot Booper",
        secretMessage = "BOOP x5! Maximum tail-wag velocity reached. All barks now carry +100% chaotic good-boy energy!",
        crypticHint = "Try booping the tiny dog badge at the top of the app 5 times in a row..."
    ),
    LEGENDARY_DOG_NAME(
        id = "legendary_dog_name",
        title = "Mythic Hound Discovered!",
        emoji = "👑🐕",
        badgeLabel = "Lore Master",
        secretMessage = "You registered a legendary canine icon! BarkTalk's acoustic translator bows in respect.",
        crypticHint = "Name a dog profile after a famous pop-culture or mythic hound (Doge, Scooby, Snoopy, Clifford, Cerberus...)."
    ),
    SQUIRREL_DEFCON(
        id = "squirrel_defcon",
        title = "DEFCON 1: SQUIRREL ALERT!",
        emoji = "🐿️🚨",
        badgeLabel = "Backyard Sentinel",
        secretMessage = "RED ALERT! Fluffy-tailed perimeter threat detected on the cedar fence! Commencing tactical bark protocol!",
        crypticHint = "Tap your active dog's avatar 5 times on the Translate hero card..."
    ),
    SOUNDBOARD_OPERA(
        id = "soundboard_opera",
        title = "Midnight Bark Opera!",
        emoji = "🐺🎵",
        badgeLabel = "Canine Maestro",
        secretMessage = "AWOOOOO! You unlocked the secret Bark Konami Symphony! Every dog within 3 blocks is harmonizing with you.",
        crypticHint = "Play Happy Greeting → Play Bow → Warning Guard in sequence on the Soundboard (or tap its speaker icon 5 times)."
    )
}

/**
 * Result when a user enters a secret legendary dog name or breed in a pet profile.
 */
data class SecretDogLegend(
    val canonicalName: String,
    val emoji: String,
    val title: String,
    val quip: String
)

/**
 * Active celebration event shown in the overlay when an Easter egg is triggered.
 */
data class EasterEggCelebrationEvent(
    val egg: BarkEasterEgg,
    val customMessage: String? = null,
    val isFirstDiscovery: Boolean = false
)

/**
 * Pure logic and persistence helpers for BarkTalk's hidden Easter eggs.
 */
object EasterEggEngine {
    private const val PREF_KEY_UNLOCKED = "unlocked_easter_eggs"
    private const val PREF_KEY_ZOOMIES = "zoomies_mode_enabled"

    val SECRET_SOUNDBOARD_COMBO: List<String> = listOf(
        "happy_greeting",
        "playful_bow",
        "warning_guard"
    )

    private val SECRET_LEGENDS = listOf(
        Pair(
            listOf("doge", "kabosu", "shibe"),
            SecretDogLegend(
                canonicalName = "Doge",
                emoji = "🪙🐕",
                title = "Much Legend. Very Bark.",
                quip = "Such acoustic. Many decibel. Very translate. Wow."
            )
        ),
        Pair(
            listOf("scooby", "scooby-doo", "scooby doo"),
            SecretDogLegend(
                canonicalName = "Scooby-Doo",
                emoji = "🍪🔎",
                title = "Mystery Machine Canine!",
                quip = "Ruh-roh! Scooby Snack sonar calibrated to 110%!"
            )
        ),
        Pair(
            listOf("clifford"),
            SecretDogLegend(
                canonicalName = "Clifford",
                emoji = "🔴🐾",
                title = "The Big Red Dog!",
                quip = "Warning: 25-foot-tall sub-bass bark may register on local seismographs!"
            )
        ),
        Pair(
            listOf("snoopy"),
            SecretDogLegend(
                canonicalName = "Snoopy",
                emoji = "✈️🐶",
                title = "World-Famous Flying Ace!",
                quip = "Doghouse radar online. Watching out for the Red Baron and suppertime."
            )
        ),
        Pair(
            listOf("cerberus"),
            SecretDogLegend(
                canonicalName = "Cerberus",
                emoji = "🔥🐺",
                title = "Guardian of the Underworld!",
                quip = "Triple-headed polyphonic bark harmonics detected! Requires 3x belly rubs."
            )
        ),
        Pair(
            listOf("air bud", "airbud"),
            SecretDogLegend(
                canonicalName = "Air Bud",
                emoji = "🏀🐕",
                title = "MVP Golden Retriever!",
                quip = "Ain't no rule says a dog can't translate his own barks!"
            )
        ),
        Pair(
            listOf("lassie"),
            SecretDogLegend(
                canonicalName = "Lassie",
                emoji = "🌟🐕",
                title = "Heroic Rescue Collie!",
                quip = "What's that, girl? Timmy fell down the well again?! Translating rescue coordinates!"
            )
        ),
        Pair(
            listOf("goodest boy", "goodest girl", "best boy", "best girl"),
            SecretDogLegend(
                canonicalName = "The Goodest Pup",
                emoji = "🏆🐾",
                title = "15/10 Certified Good Pup!",
                quip = "Acoustic analysis confirms: 100% pure heart, zero flaws, deserves extra cheese."
            )
        )
    )

    /**
     * Checks whether a dog's name or breed matches a hidden pop-culture or mythic dog legend.
     */
    fun detectSecretDogLegend(name: String, breed: String = ""): SecretDogLegend? {
        val normalizedName = name.trim().lowercase()
        val normalizedBreed = breed.trim().lowercase()
        if (normalizedName.isBlank() && normalizedBreed.isBlank()) return null

        for ((keywords, legend) in SECRET_LEGENDS) {
            if (keywords.any { kw ->
                    normalizedName == kw ||
                        normalizedName.contains(kw) ||
                        normalizedBreed.contains(kw)
                }
            ) {
                return legend
            }
        }
        return null
    }

    /**
     * Returns true if the tail of [recentSoundIds] matches [SECRET_SOUNDBOARD_COMBO].
     */
    fun matchesSoundboardCombo(recentSoundIds: List<String>): Boolean {
        if (recentSoundIds.size < SECRET_SOUNDBOARD_COMBO.size) return false
        val tail = recentSoundIds.takeLast(SECRET_SOUNDBOARD_COMBO.size)
        return tail == SECRET_SOUNDBOARD_COMBO
    }

    fun loadUnlockedEggs(prefs: SharedPreferences): Set<BarkEasterEgg> {
        val raw = prefs.getStringSet(PREF_KEY_UNLOCKED, emptySet()) ?: emptySet()
        return BarkEasterEgg.entries.filter { it.id in raw }.toSet()
    }

    fun saveUnlockedEggs(prefs: SharedPreferences, eggs: Set<BarkEasterEgg>) {
        prefs.edit()
            .putStringSet(PREF_KEY_UNLOCKED, eggs.map { it.id }.toSet())
            .apply()
    }

    fun loadZoomiesMode(prefs: SharedPreferences): Boolean {
        return prefs.getBoolean(PREF_KEY_ZOOMIES, false)
    }

    fun saveZoomiesMode(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_ZOOMIES, enabled).apply()
    }
}
