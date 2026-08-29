package com.cotx.app.util

object ExamSubjectHelper {
    val TYT_AYT_SUBJECTS = listOf("Fizik", "Kimya", "Biyoloji", "Matematik", "Geometri", "Türkçe", "Coğrafya", "Tarih")
    val LGS_SUBJECTS = listOf("Türkçe", "Matematik", "Fen Bilgisi", "Sosyal Bilgiler")

    fun getSubjectsForExam(examType: String?): List<String> {
        return when (examType) {
            "LGS" -> LGS_SUBJECTS
            else -> TYT_AYT_SUBJECTS // Default to TYT/AYT
        }
    }

    fun getFeedSubjectsForExam(examType: String?): List<String> {
        return listOf("Tümü") + getSubjectsForExam(examType)
    }
}
