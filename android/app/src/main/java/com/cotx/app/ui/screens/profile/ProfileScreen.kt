package com.cotx.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cotx.app.data.model.Question
import com.cotx.app.data.model.User
import com.cotx.app.data.repository.AuthRepository
import com.cotx.app.data.repository.QuestionRepository
import com.cotx.app.ui.components.CotxBottomBar
import com.cotx.app.ui.components.CotxBottomTab
import com.cotx.app.ui.screens.feed.QuestionCard
import com.cotx.app.ui.theme.PrimaryPurple
import com.cotx.app.ui.theme.SecondaryOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUserId: String,
    targetUserId: String? = null,
    unreadNotificationCount: Int = 0,
    unreadMessageCount: Int = 0,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToExplore: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToProfile: (userId: String?) -> Unit = {},
    onNavigateToDM: (receiverId: String, receiverName: String) -> Unit,
    onNavigateToDMList: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToQuestionDetail: (questionId: String) -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val questionRepository = remember { QuestionRepository() }

    val scope = rememberCoroutineScope()

    val effectiveTargetUserId = if (targetUserId.isNullOrEmpty()) currentUserId else targetUserId
    val isOwnProfile = effectiveTargetUserId == currentUserId

    // The acting/session identity (uid, displayName, photoUrl) used to attribute
    // follow/like/report mutations - fetched here rather than passed in from NavGraph.
    var myUser by remember(currentUserId) { mutableStateOf(User(uid = currentUserId, displayName = "Öğrenci")) }
    var displayedUser by remember(effectiveTargetUserId) { mutableStateOf(User(uid = effectiveTargetUserId, displayName = "Öğrenci")) }
    var userQuestions by remember(effectiveTargetUserId) { mutableStateOf<List<Question>>(emptyList()) }
    var isLoadingQuestions by remember(effectiveTargetUserId) { mutableStateOf(true) }

    var currentUserFollowing by remember(myUser.following) { mutableStateOf(myUser.following) }
    var currentUserBlockedList by remember(myUser.blockedUsers) { mutableStateOf(myUser.blockedUsers) }
    val isFollowingTarget = currentUserFollowing.contains(displayedUser.uid)
    val isTargetBlocked = currentUserBlockedList.contains(displayedUser.uid)

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showReportUserDialog by remember { mutableStateOf(false) }
    var showBlockConfirmationDialog by remember { mutableStateOf(false) }
    var showOtherUserMenu by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showPrivacySubmenu by remember { mutableStateOf(false) }

    // User List Modal (Takipçi / Takip Edilen)
    var showUserListModal by remember { mutableStateOf(false) }
    var activeModalTab by remember { mutableStateOf(0) } // 0: Takipçiler, 1: Takip Edilenler
    var modalFollowers by remember { mutableStateOf<List<User>>(emptyList()) }
    var modalFollowing by remember { mutableStateOf<List<User>>(emptyList()) }
    var isModalLoading by remember { mutableStateOf(false) }

    // Blocked Users Modal
    var showBlockedUsersModal by remember { mutableStateOf(false) }
    var blockedUsersList by remember { mutableStateOf<List<User>>(emptyList()) }
    var isBlockedModalLoading by remember { mutableStateOf(false) }

    // Fetch acting-user identity (for mutation attribution) + Target User Info & Questions
    LaunchedEffect(currentUserId, effectiveTargetUserId) {
        val myProfileResult = authRepository.getCurrentUserProfile()
        myProfileResult.onSuccess { fetchedUser ->
            if (fetchedUser != null) myUser = fetchedUser
        }

        if (isOwnProfile) {
            myProfileResult.onSuccess { fetchedUser ->
                if (fetchedUser != null) {
                    displayedUser = fetchedUser
                    currentUserFollowing = fetchedUser.following
                    currentUserBlockedList = fetchedUser.blockedUsers
                }
            }
        } else {
            authRepository.getUserProfile(effectiveTargetUserId).onSuccess { fetchedUser ->
                if (fetchedUser != null) {
                    displayedUser = fetchedUser
                }
            }
        }
        questionRepository.getUserQuestions(effectiveTargetUserId).onSuccess { questions ->
            userQuestions = questions
            isLoadingQuestions = false
        }.onFailure {
            isLoadingQuestions = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(displayedUser.displayName.ifEmpty { "Profil" }, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (isOwnProfile) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = {
                                    showMenu = false
                                    showPrivacySubmenu = false
                                }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Profili Düzenle") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToEditProfile()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        onLogout()
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Gizlilik ve İzinler") },
                                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                                    trailingIcon = { Icon(Icons.Default.ArrowRight, contentDescription = null) },
                                    onClick = {
                                        showPrivacySubmenu = !showPrivacySubmenu
                                    }
                                )
                                if (showPrivacySubmenu) {
                                    DropdownMenuItem(
                                        text = { Text("Engellenen Kullanıcılar") },
                                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) },
                                        onClick = {
                                            showMenu = false
                                            showPrivacySubmenu = false
                                            showBlockedUsersModal = true
                                            isBlockedModalLoading = true
                                            scope.launch {
                                                authRepository.getUsersByIds(currentUserBlockedList).onSuccess { list ->
                                                    blockedUsersList = list
                                                    isBlockedModalLoading = false
                                                }.onFailure {
                                                    isBlockedModalLoading = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Hesabı Sil", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showMenu = false
                                            showPrivacySubmenu = false
                                            showDeleteConfirmation = true
                                        },
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }
                    } else if (currentUserId.isNotEmpty()) {
                        // Other user's profile - 3-line hamburger menu
                        Box {
                            IconButton(onClick = { showOtherUserMenu = true }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menü")
                            }
                            DropdownMenu(
                                expanded = showOtherUserMenu,
                                onDismissRequest = { showOtherUserMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Kullanıcıyı Bildir") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    onClick = {
                                        showOtherUserMenu = false
                                        showReportUserDialog = true
                                    }
                                )
                                if (isTargetBlocked) {
                                    DropdownMenuItem(
                                        text = { Text("Engeli Kaldır") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showOtherUserMenu = false
                                            scope.launch {
                                                authRepository.unblockUser(currentUserId, displayedUser.uid).onSuccess {
                                                    currentUserBlockedList = currentUserBlockedList - displayedUser.uid
                                                    android.widget.Toast.makeText(context, "Kullanıcının engeli kaldırıldı", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("Kullanıcıyı Engelle", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Block,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            showOtherUserMenu = false
                                            showBlockConfirmationDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isOwnProfile) {
                CotxBottomBar(
                    currentTab = CotxBottomTab.PROFILE,
                    unreadNotificationCount = unreadNotificationCount,
                    unreadMessageCount = unreadMessageCount,
                    onTabSelected = { tab ->
                        when (tab) {
                            CotxBottomTab.EXPLORE -> onNavigateToExplore()
                            CotxBottomTab.HOME -> onNavigateToHome()
                            CotxBottomTab.MESSAGES -> onNavigateToDMList()
                            CotxBottomTab.NOTIFICATIONS -> onNavigateToNotifications()
                            CotxBottomTab.PROFILE -> {}
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Profile Header Box
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (displayedUser.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = com.cotx.app.util.ImageModelResolver.resolve(displayedUser.photoUrl),
                            contentDescription = "Profil Fotoğrafı",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayedUser.displayName.take(1).uppercase().ifEmpty { "Ö" },
                                style = MaterialTheme.typography.headlineLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = displayedUser.displayName.ifEmpty { "Öğrenci" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    if (displayedUser.field.isNotEmpty()) {
                        Text(
                            text = displayedUser.field,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (displayedUser.bio.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = displayedUser.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instagram Style Counters (Sorular | Takipçi | Takip Edilen)
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${userQuestions.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(text = "Sorular", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        VerticalDivider(modifier = Modifier.height(24.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                activeModalTab = 0
                                showUserListModal = true
                                isModalLoading = true
                                scope.launch {
                                    authRepository.getUsersByIds(displayedUser.followers).onSuccess { modalFollowers = it }
                                    authRepository.getUsersByIds(displayedUser.following).onSuccess { modalFollowing = it }
                                    isModalLoading = false
                                }
                            }
                        ) {
                            Text(
                                text = "${displayedUser.followers.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(text = "Takipçi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        VerticalDivider(modifier = Modifier.height(24.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                activeModalTab = 1
                                showUserListModal = true
                                isModalLoading = true
                                scope.launch {
                                    authRepository.getUsersByIds(displayedUser.followers).onSuccess { modalFollowers = it }
                                    authRepository.getUsersByIds(displayedUser.following).onSuccess { modalFollowing = it }
                                    isModalLoading = false
                                }
                            }
                        ) {
                            Text(
                                text = "${displayedUser.following.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(text = "Takip Edilen", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Action buttons for other user's profile
                    if (!isOwnProfile) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(0.95f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isTargetBlocked) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            authRepository.unblockUser(currentUserId, displayedUser.uid).onSuccess {
                                                currentUserBlockedList = currentUserBlockedList - displayedUser.uid
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Engeli Kaldır", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Follow / Unfollow Button
                                Button(
                                    onClick = {
                                        scope.launch {
                                            if (isFollowingTarget) {
                                                authRepository.unfollowUser(currentUserId, displayedUser.uid).onSuccess {
                                                    currentUserFollowing = currentUserFollowing - displayedUser.uid
                                                    displayedUser = displayedUser.copy(followers = displayedUser.followers - currentUserId)
                                                }
                                            } else {
                                                 authRepository.followUser(
                                                     currentUserId = currentUserId,
                                                     targetUserId = displayedUser.uid,
                                                     currentUserName = myUser.displayName,
                                                     currentUserPhotoUrl = myUser.photoUrl
                                                 ).onSuccess {
                                                    currentUserFollowing = currentUserFollowing + displayedUser.uid
                                                    displayedUser = displayedUser.copy(followers = displayedUser.followers + currentUserId)
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowingTarget) MaterialTheme.colorScheme.surfaceVariant else PrimaryPurple
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        if (isFollowingTarget) "Takipte" else "+ Takip Et",
                                        color = if (isFollowingTarget) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Message Button
                                OutlinedButton(
                                    onClick = { onNavigateToDM(displayedUser.uid, displayedUser.displayName) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = SecondaryOrange)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mesaj", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Target Box
            if (displayedUser.targetUniversity.isNotEmpty() || displayedUser.targetMajor.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryPurple.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Hedef", tint = SecondaryOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (displayedUser.examType == "LGS") "Hedef Lise" else "Sınav / Üniversite Hedefi",
                                    style = MaterialTheme.typography.titleSmall.copy(color = SecondaryOrange, fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = listOf(displayedUser.targetUniversity, displayedUser.targetMajor).filter { it.isNotEmpty() }.joinToString(" - "),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // 3. Visible Badges Section
            val badgesToShow = if (displayedUser.visibleBadges != null) {
                displayedUser.visibleBadges!!
            } else {
                displayedUser.badges
            }
            if (badgesToShow.isNotEmpty()) {
                item {
                    Column {
                        Text("Rozetler 🏅", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(badgesToShow) { badgeName ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = SecondaryOrange.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = badgeName,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(color = SecondaryOrange, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Posted Questions Section
            item {
                Text(
                    text = if (isOwnProfile) "Paylaştığım Sorular (${userQuestions.size})" else "${displayedUser.displayName} Tarafından Paylaşılan Sorular (${userQuestions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (isLoadingQuestions) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                }
            } else if (userQuestions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Henüz paylaşılan soru bulunmuyor.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(userQuestions) { question ->
                    QuestionCard(
                        question = question,
                        currentUserId = currentUserId,
                        isFollowingAuthor = currentUserFollowing.contains(question.authorId),
                        onCardClick = { onNavigateToQuestionDetail(question.id) },
                        onLikeClick = {
                            val isLiked = question.likedBy.contains(currentUserId)
                            scope.launch {
                                questionRepository.toggleLikeQuestion(
                                    questionId = question.id,
                                    userId = currentUserId,
                                    userName = myUser.displayName,
                                    userPhotoUrl = myUser.photoUrl,
                                    isLiked = isLiked
                                )
                                questionRepository.getUserQuestions(effectiveTargetUserId).onSuccess { userQuestions = it }
                            }
                        },
                        onFollowToggleClick = {
                            scope.launch {
                                if (currentUserFollowing.contains(question.authorId)) {
                                    authRepository.unfollowUser(currentUserId, question.authorId).onSuccess {
                                        currentUserFollowing = currentUserFollowing - question.authorId
                                    }
                                } else {
                                     authRepository.followUser(
                                         currentUserId = currentUserId,
                                         targetUserId = question.authorId,
                                         currentUserName = myUser.displayName,
                                         currentUserPhotoUrl = myUser.photoUrl
                                     ).onSuccess {
                                        currentUserFollowing = currentUserFollowing + question.authorId
                                    }
                                }
                            }
                        },
                        onDeleteQuestionClick = if (question.authorId == currentUserId) {
                            {
                                scope.launch {
                                    questionRepository.deleteQuestion(question.id, currentUserId).onSuccess {
                                        questionRepository.getUserQuestions(effectiveTargetUserId).onSuccess { userQuestions = it }
                                    }
                                }
                            }
                        } else null
                    )
                }
            }
        }
    }

    // --- Modal Dialog for Followers / Following Dual-Tab List ---
    if (showUserListModal) {
        AlertDialog(
            onDismissRequest = { showUserListModal = false },
            title = {
                TabRow(
                    selectedTabIndex = activeModalTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PrimaryPurple
                ) {
                    Tab(
                        selected = activeModalTab == 0,
                        onClick = { activeModalTab = 0 },
                        text = {
                            Text(
                                text = "Takipçiler (${displayedUser.followers.size})",
                                fontWeight = if (activeModalTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = activeModalTab == 1,
                        onClick = { activeModalTab = 1 },
                        text = {
                            Text(
                                text = "Takip Edilen (${displayedUser.following.size})",
                                fontWeight = if (activeModalTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    if (isModalLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryPurple)
                    } else {
                        val currentList = if (activeModalTab == 0) modalFollowers else modalFollowing
                        if (currentList.isEmpty()) {
                            Text(
                                text = if (activeModalTab == 0) "Henüz takipçi bulunmuyor." else "Henüz takip edilen bulunmuyor.",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(currentList) { targetUser ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                showUserListModal = false
                                                if (targetUser.uid != currentUserId) {
                                                    onNavigateToProfile(targetUser.uid)
                                                } else {
                                                    onNavigateToProfile(null)
                                                }
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryPurple.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (targetUser.photoUrl.isNotEmpty()) {
                                                AsyncImage(
                                                    model = com.cotx.app.util.ImageModelResolver.resolve(targetUser.photoUrl),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(
                                                    text = targetUser.displayName.ifEmpty { "Ö" }.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryPurple,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = targetUser.displayName.ifEmpty { "Öğrenci" },
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            if (targetUser.targetUniversity.isNotEmpty() || targetUser.targetMajor.isNotEmpty()) {
                                                Text(
                                                    text = listOf(targetUser.targetUniversity, targetUser.targetMajor).filter { it.isNotEmpty() }.joinToString(" - "),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserListModal = false }) {
                    Text("Kapat", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- Modal Dialog for Blocked Users List ---
    if (showBlockedUsersModal) {
        AlertDialog(
            onDismissRequest = { showBlockedUsersModal = false },
            title = { Text("Engellenen Kullanıcılar", fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    if (isBlockedModalLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryPurple)
                    } else if (blockedUsersList.isEmpty()) {
                        Text(
                            text = "Engellenen kullanıcı bulunmuyor.",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(blockedUsersList) { blockedUser ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryPurple.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = blockedUser.displayName.ifEmpty { "Ö" }.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryPurple
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = blockedUser.displayName.ifEmpty { "Öğrenci" },
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                authRepository.unblockUser(currentUserId, blockedUser.uid).onSuccess {
                                                    currentUserBlockedList = currentUserBlockedList - blockedUser.uid
                                                    blockedUsersList = blockedUsersList.filter { it.uid != blockedUser.uid }
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Engeli Kaldır", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedUsersModal = false }) {
                    Text("Kapat", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Hesabı Sil", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text("Hesabınızı silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Sil", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    // Block User Confirmation Dialog
    if (showBlockConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmationDialog = false },
            title = { Text("Kullanıcıyı Engelle", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text("${displayedUser.displayName.ifEmpty { "Bu kullanıcıyı" }} engellemek istediğinize emin misiniz? Engellediğinizde birbirinizin paylaşımlarını ve mesajlarını göremezsiniz.") },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockConfirmationDialog = false
                        scope.launch {
                            authRepository.blockUser(currentUserId, displayedUser.uid).onSuccess {
                                currentUserBlockedList = currentUserBlockedList + displayedUser.uid
                                currentUserFollowing = currentUserFollowing - displayedUser.uid
                                displayedUser = displayedUser.copy(
                                    followers = displayedUser.followers - currentUserId,
                                    following = displayedUser.following - currentUserId
                                )
                                android.widget.Toast.makeText(context, "Kullanıcı engellendi", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Engelle", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmationDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    // Report User Dialog
    if (showReportUserDialog) {
        com.cotx.app.ui.components.ReportDialog(
            title = "Kullanıcıyı Bildir 🚩",
            onDismissRequest = { showReportUserDialog = false },
            onConfirmReport = { reason, note ->
                showReportUserDialog = false
                scope.launch {
                    authRepository.reportUser(
                        targetUserId = displayedUser.uid,
                        reporterId = currentUserId,
                        reason = reason,
                        note = note
                    ).onSuccess {
                        android.widget.Toast.makeText(context, "Kullanıcı bildirildi. İncelemeye alındı.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

