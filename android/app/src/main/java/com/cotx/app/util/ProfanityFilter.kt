package com.cotx.app.util

import java.util.Locale

object ProfanityFilter {
    private val badWords = listOf(
        "amk", "aq", "oç", "oc", "sik", "sikis", "sikiş", "yarrak", "yarak",
        "piç", "pic", "göt", "got", "orospu", "ibne", "fuck", "bitch", "shit"
    )

    fun containsProfanity(text: String): Boolean {
        if (text.isBlank()) return false
        val normalized = text.lowercase(Locale("tr"))
            .replace("@", "a")
            .replace("0", "o")
            .replace("1", "i")
            .replace("3", "e")

        val words = normalized.split(Regex("\\s+|[.,!?:;\\-_]+"))
        return words.any { word ->
            badWords.any { bad -> word == bad || word.contains(bad) }
        }
    }
}