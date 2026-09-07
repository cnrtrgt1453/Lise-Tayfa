package com.cotx.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cotx.app.data.model.User
import com.cotx.app.domain.model.UserSummary
import com.cotx.app.ui.screens.auth.LoginScreen
import com.cotx.app.ui.screens.auth.RegisterScreen
import com.cotx.app.ui.screens.chat.DirectMessageScreen
import com.cotx.app.ui.screens.chat.MessagesMainScreen
import com.cotx.app.ui.screens.feed.FeedScreen
import com.cotx.app.ui.screens.notification.NotificationScreen
import com.cotx.app.ui.screens.profile.EditProfileScreen
import com.cotx.app.ui.screens.profile.ProfileScreen
import com.cotx.app.ui.screens.question.AddQuestionScreen
import com.cotx.app.ui.screens.question.QuestionDetailScreen
import com.cotx.app.viewmodel.AddQuestionViewModel
import com.cotx.app.viewmodel.AuthViewModel
import com.cotx.app.viewmodel.ChatViewModel
import com.cotx.app.viewmodel.FeedTab
import com.cotx.app.viewmodel.FeedViewModel
import com.cotx.app.viewmodel.NotificationViewModel

@Composable
fun CotxNavGraph(
    navController: NavHostController,
    initialQuestionId: String? = null,
    authViewModel: AuthViewModel = viewModel(),
    feedViewModel: FeedViewModel = viewModel(),
    addQuestionViewModel: AddQuestionViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState()
    val unreadMessageCount by chatViewModel.unreadMessageCount.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.runtime.LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid ?: ""
        if (uid.isNotEmpty()) {
            chatViewModel.initListeners(uid)
            notificationViewModel.initRealtimeListener(uid, context)
        }
    }

    androidx.compose.runtime.LaunchedEffect(currentUser, initialQuestionId) {
        if (currentUser != null && !initialQuestionId.isNullOrEmpty()) {
            navController.navigate(Screen.QuestionDetail.createRoute(initialQuestionId))
        }
    }

    val startDestination = if (currentUser != null) Screen.Feed.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Feed.route) {
            val user = currentUser ?: User(displayName = "Öğrenci")
            androidx.compose.runtime.LaunchedEffect(user.uid) {
                if (user.uid.isNotEmpty()) {
                    notificationViewModel.loadNotifications(user.uid)
                }
            }

            FeedScreen(
                viewModel = feedViewModel,
                currentUser = UserSummary(id = user.uid, displayName = user.displayName, avatarUrl = user.photoUrl),
                examType = user.examType,
                myFollowingUserIds = user.following,
                myBlockedUserIds = user.blockedUsers,
                unreadNotificationCount = unreadNotificationCount,
                unreadMessageCount = unreadMessageCount,
                onExamSelected = { selectedExam ->
                    authViewModel.updateExamType(selectedExam)
                },
                onNavigateToAddQuestion = { navController.navigate(Screen.AddQuestion.route) },
                onNavigateToQuestionDetail = { questionId ->
                    navController.navigate(Screen.QuestionDetail.createRoute(questionId))
                },
                onNavigateToProfile = { targetUserId ->
                    navController.navigate(Screen.Profile.createRoute(targetUserId))
                },
                onNavigateToNotifications = {
                    if (user.uid.isNotEmpty()) notificationViewModel.markAllAsRead(user.uid)
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToDMList = {
                    if (user.uid.isNotEmpty()) chatViewModel.markAllConversationsAsRead(user.uid)
                    navController.navigate(Screen.Messages.route)
                }
            )
        }

        composable(Screen.AddQuestion.route) {
            val user = currentUser ?: User(displayName = "Öğrenci")
            AddQuestionScreen(
                viewModel = addQuestionViewModel,
                currentUser = UserSummary(id = user.uid, displayName = user.displayName, avatarUrl = user.photoUrl),
                examType = user.examType,
                onNavigateBack = { navController.popBackStack() },
                onQuestionUploaded = {
                    feedViewModel.loadFeed()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.QuestionDetail.route,
            arguments = listOf(navArgument("questionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            val user = currentUser ?: User(displayName = "Öğrenci")
            QuestionDetailScreen(
                questionId = questionId,
                currentUser = UserSummary(id = user.uid, displayName = user.displayName, avatarUrl = user.photoUrl),
                followingUserIds = user.following,
                onNavigateToProfile = { targetUserId ->
                    navController.navigate(Screen.Profile.createRoute(targetUserId))
                },
                onNavigateToExplore = {
                    feedViewModel.selectTab(FeedTab.EXPLORE)
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DirectMessage.route,
            arguments = listOf(
                navArgument("receiverId") { type = NavType.StringType },
                navArgument("receiverName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            val receiverName = backStackEntry.arguments?.getString("receiverName") ?: "Öğrenci"
            val user = currentUser ?: User(displayName = "Öğrenci")
            DirectMessageScreen(
                receiverId = receiverId,
                receiverName = receiverName,
                currentUser = UserSummary(id = user.uid, displayName = user.displayName, avatarUrl = user.photoUrl),
                isBlockedByMe = user.blockedUsers.contains(receiverId),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Messages.route) {
            val user = currentUser ?: User(displayName = "Öğrenci")
            MessagesMainScreen(
                viewModel = chatViewModel,
                currentUser = UserSummary(id = user.uid, displayName = user.displayName, avatarUrl = user.photoUrl),
                unreadNotificationCount = unreadNotificationCount,
                unreadMessageCount = unreadMessageCount,
                onNavigateToPrivateDM = { receiverId, receiverName ->
                    navController.navigate(Screen.DirectMessage.createRoute(receiverId, receiverName))
                },
                onNavigateToExplore = {
                    feedViewModel.selectTab(FeedTab.EXPLORE)
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    feedViewModel.selectTab(FeedTab.FOLLOWING)
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                onNavigateToNotifications = {
                    if (user.uid.isNotEmpty()) notificationViewModel.markAllAsRead(user.uid)
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToProfile = { targetUserId ->
                    navController.navigate(Screen.Profile.createRoute(targetUserId))
                }
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val targetUserId = backStackEntry.arguments?.getString("userId")
            val myUserId = currentUser?.uid ?: ""

            ProfileScreen(
                currentUserId = myUserId,
                targetUserId = targetUserId,
                unreadNotificationCount = unreadNotificationCount,
                unreadMessageCount = unreadMessageCount,
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToExplore = {
                    feedViewModel.selectTab(FeedTab.EXPLORE)
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    feedViewModel.selectTab(FeedTab.FOLLOWING)
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = { userId ->
                    navController.navigate(Screen.Profile.createRoute(userId))
                },
                onNavigateToDM = { receiverId, receiverName ->
                    navController.navigate(Screen.DirectMessage.createRoute(receiverId, receiverName))
                },
                onNavigateToDMList = {
                    if (myUserId.isNotEmpty()) chatViewModel.markAllConversationsAsRead(myUserId)
                    navController.navigate(Screen.Messages.route)
                },
                onNavigateToNotifications = {
                    if (myUserId.isNotEmpty()) notificationViewModel.markAllAsRead(myUserId)
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToQuestionDetail = { questionId ->
                    navController.navigate(Screen.QuestionDetail.createRoute(questionId))
                },
                onDeleteAccount = {
                    authViewModel.deleteAccount {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            val myUserId = currentUser?.uid ?: ""
            val context = androidx.compose.ui.platform.LocalContext.current

            EditProfileScreen(
                userId = myUserId,
                onUpdateProfile = { displayName, bio, field, examType, targetSchool, targetDepartment, visibleBadges, newImageUri, onFinished ->
                    authViewModel.updateProfile(
                        context = context,
                        displayName = displayName,
                        bio = bio,
                        field = field,
                        examType = examType,
                        targetUniversity = targetSchool,
                        targetMajor = targetDepartment,
                        visibleBadges = visibleBadges,
                        imageUri = newImageUri,
                        onSuccess = {
                            onFinished()
                            navController.popBackStack()
                        },
                        onError = {
                            onFinished()
                        }
                    )
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            val myUserId = currentUser?.uid ?: ""
            androidx.compose.runtime.LaunchedEffect(myUserId) {
                if (myUserId.isNotEmpty()) {
                    notificationViewModel.markAllAsRead(myUserId)
                }
            }

            NotificationScreen(
                viewModel = notificationViewModel,
                currentUserId = myUserId,
                unreadNotificationCount = unreadNotificationCount,
                unreadMessageCount = unreadMessageCount,
                onNavigateToQuestionDetail = { questionId ->
                    navController.navigate(Screen.QuestionDetail.createRoute(questionId))
                },
                onNavigateToExplore = {
                    feedViewModel.selectTab(FeedTab.EXPLORE)
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    feedViewModel.selectTab(FeedTab.FOLLOWING)
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                onNavigateToDMList = {
                    if (myUserId.isNotEmpty()) chatViewModel.markAllConversationsAsRead(myUserId)
                    navController.navigate(Screen.Messages.route)
                },
                onNavigateToProfile = { targetUserId ->
                    navController.navigate(Screen.Profile.createRoute(targetUserId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
