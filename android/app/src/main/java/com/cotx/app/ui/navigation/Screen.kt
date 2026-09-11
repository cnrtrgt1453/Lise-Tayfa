package com.cotx.app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Feed : Screen("feed")
    object AddQuestion : Screen("add_question")
    object QuestionDetail : Screen("question_detail/{questionId}") {
        fun createRoute(questionId: String) = "question_detail/$questionId"
    }
    object DirectMessage : Screen("direct_message/{receiverId}/{receiverName}") {
        fun createRoute(receiverId: String, receiverName: String) = "direct_message/$receiverId/$receiverName"
    }
    object Profile : Screen("profile?userId={userId}") {
        fun createRoute(userId: String? = null) = if (!userId.isNullOrEmpty()) "profile?userId=$userId" else "profile"
    }
    object EditProfile : Screen("edit_profile")
    object Notifications : Screen("notifications")
    object Messages : Screen("messages")
    object ReportUser : Screen("report_user/{targetUserId}?userName={userName}") {
        fun createRoute(targetUserId: String, userName: String = ""): String {
            val encodedName = if (userName.isNotEmpty()) {
                try {
                    java.net.URLEncoder.encode(userName, "UTF-8")
                } catch (_: Exception) {
                    userName
                }
            } else ""
            return "report_user/$targetUserId?userName=$encodedName"
        }
    }
    object FollowList : Screen("follow_list/{userId}?initialTab={initialTab}") {
        fun createRoute(userId: String, initialTab: Int = 0) = "follow_list/$userId?initialTab=$initialTab"
    }
    object LikedUsers : Screen("liked_users/{questionId}") {
        fun createRoute(questionId: String) = "liked_users/$questionId"
    }
    object BlockedUsers : Screen("blocked_users")
}
