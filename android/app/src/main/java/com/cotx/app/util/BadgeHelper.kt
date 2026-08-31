package com.cotx.app.util

data class SubjectBadge(
    val title: String,
    val description: String,
    val emoji: String,
    val level: String, // "BRONZE", "SILVER", "GOLD", "LOCKED"
    val count: Int,
    val targetCount: Int
)

object BadgeHelper {
    /**
     * Computes earned subject badges and progress.
     * Tier requirements:
     * - 30+ questions: Bronz 🥉
     * - 100+ questions: Gümüş 🥈
     * - 1000+ questions: Altın 🥇
     */
    fun getEarnedBadges(subjectCounts: Map<String, Int>): List<SubjectBadge> {
        val earned = mutableListOf<SubjectBadge>()

        subjectCounts.forEach { (subject, count) ->
            when {
                count >= 1000 -> {
                    earned.add(
                        SubjectBadge(
                            title = "$subject Altın",
                            description = "$count Soru Paylaşıldı",
                            emoji = "🥇",
                            level = "GOLD",
                            count = count,
                            targetCount = 1000
                        )
                    )
                }
                count >= 100 -> {
                    earned.add(
                        SubjectBadge(
                            title = "$subject Gümüş",
                            description = "$count Soru Paylaşıldı",
                            emoji = "🥈",
                            level = "SILVER",
                            count = count,
                            targetCount = 1000
                        )
                    )
                }
                count >= 30 -> {
                    earned.add(
                        SubjectBadge(
                            title = "$subject Bronz",
                            description = "$count Soru Paylaşıldı",
                            emoji = "🥉",
                            level = "BRONZE",
                            count = count,
                            targetCount = 100
                        )
                    )
                }
            }
        }

        return earned
    }

    /**
     * Returns string names of all earned badges (subject badges + initial badges)
     */
    fun getAllEarnedBadgeNames(subjectCounts: Map<String, Int>, badges: List<String>): List<String> {
        val list = mutableListOf<String>()
        getEarnedBadges(subjectCounts).forEach { badge ->
            list.add("${badge.emoji} ${badge.title}")
        }
        badges.forEach { badge ->
            val formatted = if (badge.startsWith("🏅") || badge.startsWith("🥉") || badge.startsWith("🥈") || badge.startsWith("🥇")) badge else "🏅 $badge"
            if (!list.contains(formatted)) {
                list.add(formatted)
            }
        }
        return list
    }

    /**
     * Returns progress list for all main subjects to show how close the user is to earning badges.
     */
    fun getSubjectBadgeProgressList(subjectCounts: Map<String, Int>): List<SubjectBadge> {
        val mainSubjects = listOf("Matematik", "Fizik", "Kimya", "Biyoloji", "Türkçe", "Tarih", "Coğrafya")

        return mainSubjects.map { subject ->
            val count = subjectCounts[subject] ?: 0
            val (level, emoji, target) = when {
                count >= 1000 -> Triple("GOLD", "🥇", 1000)
                count >= 100 -> Triple("SILVER", "🥈", 1000)
                count >= 30 -> Triple("BRONZE", "🥉", 100)
                else -> Triple("LOCKED", "🔒", 30)
            }
            val title = when (level) {
                "GOLD" -> "$subject Altın"
                "SILVER" -> "$subject Gümüş"
                "BRONZE" -> "$subject Bronz"
                else -> "$subject (Kilitli)"
            }
            SubjectBadge(
                title = title,
                description = "$count / $target Soru",
                emoji = emoji,
                level = level,
                count = count,
                targetCount = target
            )
        }
    }
}
