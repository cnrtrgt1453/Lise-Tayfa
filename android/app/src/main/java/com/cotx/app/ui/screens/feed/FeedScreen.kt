package com.cotx.app.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cotx.app.data.model.Question
import com.cotx.app.domain.model.UserSummary
import com.cotx.app.ui.components.CotxBottomBar
import com.cotx.app.ui.components.CotxBottomTab
import com.cotx.app.ui.components.ExamSelectionDialog
import com.cotx.app.ui.theme.*
import com.cotx.app.viewmodel.FeedTab
import com.cotx.app.viewmodel.FeedUiState
import com.cotx.app.viewmodel.FeedViewModel
import com.cotx.app.viewmodel.SortOrder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    currentUser: UserSummary,
    examType: String,
    myFollowingUserIds: List<String>,
    myBlockedUserIds: List<String>,
    unreadNotificationCount: Int = 0,
    unreadMessageCount: Int = 0,
    onExamSelected: (String) -> Unit,
    onNavigateToAddQuestion: () -> Unit,
    onNavigateToQuestionDetail: (String) -> Unit,
    onNavigateToProfile: (userId: String?) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToDMList: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val followingUserIds by viewModel.followingUserIds.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showSubjectMenu by remember { mutableStateOf(false) }
    var questionToReport by remember { mutableStateOf<Question?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(examType) {
        if (examType.isNotEmpty()) {
            viewModel.updateSubjectsForExam(examType)
        }
    }

    LaunchedEffect(myFollowingUserIds) {
        viewModel.updateFollowingList(myFollowingUserIds)
    }

    LaunchedEffect(myBlockedUserIds) {
        viewModel.updateBlockedList(myBlockedUserIds)
    }

    if (examType.isEmpty() && currentUser.id.isNotEmpty()) {
        ExamSelectionDialog(
            onExamSelected = { selectedExam ->
                onExamSelected(selectedExam)
                viewModel.updateSubjectsForExam(selectedExam)
            }
        )
    }

    val currentBottomTab = if (selectedTab == FeedTab.EXPLORE) CotxBottomTab.EXPLORE else CotxBottomTab.HOME

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.cotx.app.R.drawable.app_logo),
                            contentDescription = "cotx Academy Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "cotx Academy",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            CotxBottomBar(
                currentTab = currentBottomTab,
                unreadNotificationCount = unreadNotificationCount,
                unreadMessageCount = unreadMessageCount,
                onTabSelected = { tab ->
                    when (tab) {
                        CotxBottomTab.EXPLORE -> viewModel.selectTab(FeedTab.EXPLORE)
                        CotxBottomTab.HOME -> viewModel.selectTab(FeedTab.FOLLOWING)
                        CotxBottomTab.MESSAGES -> onNavigateToDMList()
                        CotxBottomTab.NOTIFICATIONS -> onNavigateToNotifications()
                        CotxBottomTab.PROFILE -> onNavigateToProfile(null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddQuestion,
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Soru Sor")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filtre ve Sıralama Barı (Listenin Başı - Listbox Dropdown Menüler)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tarih Sıralaması Listbox Dropdown
                Box {
                    AssistChip(
                        onClick = { showSortMenu = true },
                        label = {
                            Text(
                                if (sortOrder == SortOrder.NEWEST_FIRST) "En Yeniler ⬇" else "En Eskiler ⬆",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.SwapVert,
                                contentDescription = "Sırala",
                                modifier = Modifier.size(16.dp),
                                tint = PrimaryPurple
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        border = null
                    )

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "En Yeniden En Eskiye",
                                    fontWeight = if (sortOrder == SortOrder.NEWEST_FIRST) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = PrimaryPurple) },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.NEWEST_FIRST)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "En Eskiden En Yeniye",
                                    fontWeight = if (sortOrder == SortOrder.OLDEST_FIRST) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = PrimaryPurple) },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.OLDEST_FIRST)
                                showSortMenu = false
                            }
                        )
                    }
                }

                // Ders Kategori Listbox Dropdown
                Box {
                    AssistChip(
                        onClick = { showSubjectMenu = true },
                        label = {
                            Text(
                                "Ders: $selectedSubject ▾",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = "Ders Seç",
                                modifier = Modifier.size(16.dp),
                                tint = SecondaryOrange
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = PrimaryPurple.copy(alpha = 0.12f),
                            labelColor = PrimaryPurple
                        ),
                        border = null
                    )

                    DropdownMenu(
                        expanded = showSubjectMenu,
                        onDismissRequest = { showSubjectMenu = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = subject,
                                        fontWeight = if (selectedSubject == subject) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedSubject == subject) PrimaryPurple else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = {
                                    if (selectedSubject == subject) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryPurple)
                                    } else null
                                },
                                onClick = {
                                    viewModel.selectSubject(subject)
                                    showSubjectMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Question List / State handling
            when (val state = uiState) {
                is FeedUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                }
                is FeedUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadFeed() }) {
                                Text("Tekrar Dene")
                            }
                        }
                    }
                }
                is FeedUiState.Success -> {
                    if (state.questions.isEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            item {
                                if (selectedTab == FeedTab.FOLLOWING) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PeopleOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Henüz takip ettiğin kimse yok!",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Keşfet sekmesinden diğer öğrencilerin sorularını inceleyip onları takip edebilirsin.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { viewModel.selectTab(FeedTab.EXPLORE) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Explore, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Keşfet'e Git", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "Henüz bu kategoride soru paylaşılmadı.\nİlk soruyu sen sor!",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.questions) { question ->
                                QuestionCard(
                                    question = question,
                                    currentUserId = currentUser.id,
                                    isFollowingAuthor = followingUserIds.contains(question.authorId),
                                    onCardClick = { onNavigateToQuestionDetail(question.id) },
                                    onAuthorClick = { onNavigateToProfile(question.authorId) },
                                    onLikeClick = {
                                        val isLiked = question.likedBy.contains(currentUser.id)
                                        viewModel.toggleLike(question.id, currentUser, isLiked)
                                    },
                                    onFollowToggleClick = {
                                        val isFollowing = followingUserIds.contains(question.authorId)
                                        viewModel.toggleFollowUser(
                                            currentUserId = currentUser.id,
                                            targetUserId = question.authorId,
                                            isCurrentlyFollowing = isFollowing,
                                            currentUserName = currentUser.displayName,
                                            currentUserPhotoUrl = currentUser.avatarUrl ?: ""
                                        )
                                    },
                                    onDeleteQuestionClick = {
                                        viewModel.deleteQuestion(question.id, currentUser.id)
                                    },
                                    onReportQuestionClick = {
                                        questionToReport = question
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (questionToReport != null) {
        val questionRepo = remember { com.cotx.app.data.repository.QuestionRepository() }
        com.cotx.app.ui.components.ReportDialog(
            title = "Soruyu Bildir 🚩",
            onDismissRequest = { questionToReport = null },
            onConfirmReport = { reason, note ->
                val q = questionToReport
                questionToReport = null
                if (q != null) {
                    scope.launch {
                        questionRepo.reportQuestion(
                            questionId = q.id,
                            reporterId = currentUser.id,
                            reason = reason,
                            note = note
                        ).onSuccess {
                            android.widget.Toast.makeText(context, "Soru bildirildi. İncelemeye alındı.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun QuestionCard(
    question: Question,
    currentUserId: String,
    isFollowingAuthor: Boolean,
    onCardClick: () -> Unit,
    onAuthorClick: () -> Unit = {},
    onLikeClick: () -> Unit,
    onFollowToggleClick: () -> Unit,
    onDeleteQuestionClick: (() -> Unit)? = null,
    onReportQuestionClick: (() -> Unit)? = null
) {
    val isLiked = question.likedBy.contains(currentUserId)
    var showOptionsMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Author Avatar & Name & Follow Button & Subject Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAuthorClick() }
                ) {
                    if (question.authorPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = com.cotx.app.util.ImageModelResolver.resolve(question.authorPhotoUrl),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = question.authorName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = question.authorName.ifEmpty { "Öğrenci" },
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (question.authorId.isNotEmpty() && question.authorId != currentUserId) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isFollowingAuthor) MaterialTheme.colorScheme.surfaceVariant else PrimaryPurple.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { onFollowToggleClick() }
                                ) {
                                    Text(
                                        text = if (isFollowingAuthor) "Takipte" else "+ Takip Et",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isFollowingAuthor) MaterialTheme.colorScheme.onSurfaceVariant else PrimaryPurple,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                        Text(
                            text = question.subject,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = getSubjectColor(question.subject),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Mode Badge & Optional Delete/Report Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (question.mode == "Yardım İstiyorum") SecondaryOrange.copy(alpha = 0.15f) else AccentTeal.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = question.mode,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (question.mode == "Yardım İstiyorum") SecondaryOrange else AccentTeal,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (question.authorId != currentUserId && onReportQuestionClick != null) {
                        Box {
                            IconButton(
                                onClick = { showOptionsMenu = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Seçenekler",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("İçeriği Bildir 🚩", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showOptionsMenu = false
                                        onReportQuestionClick()
                                    }
                                )
                            }
                        }
                    }

                    if (question.authorId == currentUserId && onDeleteQuestionClick != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Soruyu Sil",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Soruyu Sil 🗑️", fontWeight = FontWeight.Bold) },
                                text = { Text("Bu soruyu silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showDeleteDialog = false
                                            onDeleteQuestionClick()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Sil", fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Vazgeç")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            val displayTitle = question.displayTitle
            if (displayTitle.isNotEmpty() || question.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (displayTitle.isNotEmpty()) {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (question.description.isNotEmpty()) {
                        if (displayTitle.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Text(
                            text = question.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (displayTitle.isNotEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            maxLines = 4
                        )
                    }
                }
            }

            // Question Image
            if (question.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val imageModel: Any = remember(question.imageUrl) {
                    if (question.imageUrl.startsWith("data:image")) {
                        try {
                            val base64Data = question.imageUrl.substringAfter(",")
                            android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                        } catch (e: Exception) {
                            question.imageUrl
                        }
                    } else {
                        question.imageUrl
                    }
                }

                AsyncImage(
                    model = imageModel,
                    contentDescription = "Soru Görseli",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Likes, Date & Comments count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick() }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${question.likeCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                question.createdAt?.let { date ->
                    val dateStr = formatQuestionDate(date)
                    Text(
                        text = "📅 $dateStr",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Çözümler",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${question.commentCount} Çözüm",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

fun formatQuestionDate(date: java.util.Date): String {
    return try {
        val sdf = java.text.SimpleDateFormat("d MMMM", java.util.Locale("tr", "TR"))
        sdf.format(date)
    } catch (_: Exception) {
        ""
    }
}

fun getSubjectColor(subject: String): Color {
    return when (subject) {
        "Matematik" -> TagMatematik
        "Fizik" -> TagFizik
        "Kimya" -> TagKimya
        "Biyoloji" -> TagBiyoloji
        "Türkçe" -> TagTurkce
        else -> PrimaryPurple
    }
}
