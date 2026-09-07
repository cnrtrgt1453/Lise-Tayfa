package com.cotx.app.ui.screens.question

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cotx.app.data.model.Question
import com.cotx.app.data.model.Solution
import com.cotx.app.data.repository.QuestionRepository
import com.cotx.app.domain.model.UserSummary
import com.cotx.app.ui.screens.feed.QuestionCard
import com.cotx.app.ui.theme.AccentTeal
import com.cotx.app.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDetailScreen(
    questionId: String,
    currentUser: UserSummary,
    followingUserIds: List<String> = emptyList(),
    repository: QuestionRepository = QuestionRepository(),
    onNavigateToProfile: (userId: String) -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var question by remember { mutableStateOf<Question?>(null) }
    var solutions by remember { mutableStateOf<List<Solution>>(emptyList()) }
    var solutionText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var solutionToReport by remember { mutableStateOf<Solution?>(null) }

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            repository.getQuestionById(questionId).onSuccess { fetchedQuestion ->
                question = fetchedQuestion
            }.onFailure {
                question = null
            }
            if (question != null) {
                repository.getQuestionSolutions(questionId).onSuccess { list ->
                    solutions = list
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(questionId) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soru & Çözümler", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (question != null && question?.authorId == currentUser.id) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Soruyu Sil",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (question != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = solutionText,
                            onValueChange = { if (it.length <= 500) solutionText = it },
                            placeholder = { Text("Çözümünü veya adımını yaz...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (solutionText.isNotBlank()) {
                                    val text = solutionText
                                    solutionText = ""
                                    coroutineScope.launch {
                                        repository.addSolution(
                                            questionId = questionId,
                                            authorId = currentUser.id,
                                            authorName = currentUser.displayName,
                                            authorPhotoUrl = currentUser.avatarUrl ?: "",
                                            contentText = text
                                        )
                                        loadData()
                                    }
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryPurple)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Gönder", tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Soruyu Sil 🗑️", fontWeight = FontWeight.Bold) },
                text = { Text("Bu soruyu silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            coroutineScope.launch {
                                repository.deleteQuestion(questionId, currentUser.id).onSuccess {
                                    onNavigateBack()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Sil", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Vazgeç")
                    }
                }
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else if (question == null) {
            var showDeletedPopup by remember { mutableStateOf(true) }

            if (showDeletedPopup) {
                AlertDialog(
                    onDismissRequest = {
                        showDeletedPopup = false
                        onNavigateBack()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Soru Bulunamadı",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            text = "Bu soru sistemden silinmiştir veya süresi dolmuştur.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeletedPopup = false
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Tamam", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeletedPopup = false
                                onNavigateToExplore()
                            }
                        ) {
                            Text("Keşfet'e Git 🚀")
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bu soru silinmiştir ⚠️",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bu soru sistemden silinmiştir veya süresi dolmuştur. Keşfet sayfasındaki diğer soruları inceleyebilirsiniz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onNavigateToExplore() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Keşfet'e Git 🚀", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Question Card itself
                question?.let { currentQ ->
                    item {
                        QuestionCard(
                            question = currentQ,
                            currentUserId = currentUser.id,
                            isFollowingAuthor = followingUserIds.contains(currentQ.authorId),
                            onCardClick = {},
                            onAuthorClick = { onNavigateToProfile(currentQ.authorId) },
                            onLikeClick = {
                                val isLiked = currentQ.likedBy.contains(currentUser.id)
                                coroutineScope.launch {
                                    repository.toggleLikeQuestion(
                                        questionId = currentQ.id,
                                        userId = currentUser.id,
                                        userName = currentUser.displayName,
                                        userPhotoUrl = currentUser.avatarUrl ?: "",
                                        isLiked = isLiked
                                    )
                                    repository.getQuestionById(questionId).onSuccess { question = it }
                                }
                            },
                            onFollowToggleClick = {},
                            onDeleteQuestionClick = if (currentQ.authorId == currentUser.id) {
                                {
                                    coroutineScope.launch {
                                        repository.deleteQuestion(questionId, currentUser.id).onSuccess {
                                            onNavigateBack()
                                        }
                                    }
                                }
                            } else null
                        )
                    }
                }

                // 2. Solutions Section Header
                item {
                    Text(
                        text = "Çözümler (${solutions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // 3. Solution Items
                items(solutions) { solution ->
                    SolutionItem(
                        solution = solution,
                        currentUserId = currentUser.id,
                        onAuthorClick = { userId -> onNavigateToProfile(userId) },
                        onReportClick = { solutionToReport = solution }
                    )
                }
            }
        }

        if (solutionToReport != null) {
            com.cotx.app.ui.components.ReportDialog(
                title = "Çözümü Bildir 🚩",
                onDismissRequest = { solutionToReport = null },
                onConfirmReport = { reason, note ->
                    val targetSol = solutionToReport
                    solutionToReport = null
                    if (targetSol != null) {
                        coroutineScope.launch {
                            repository.reportSolution(
                                questionId = questionId,
                                solutionId = targetSol.id,
                                reporterId = currentUser.id,
                                reason = reason,
                                note = note
                            ).onSuccess {
                                Toast.makeText(context, "Çözüm bildirildi. İncelemeye alındı.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun SolutionItem(
    solution: Solution,
    currentUserId: String = "",
    onAuthorClick: (userId: String) -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (solution.isAcceptedAnswer) AccentTeal.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAuthorClick(solution.authorId) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        if (solution.authorPhotoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = com.cotx.app.util.ImageModelResolver.resolve(solution.authorPhotoUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = solution.authorName.ifEmpty { "Ö" }.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = solution.authorName.ifEmpty { "Öğrenci" },
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp)
                    )
                }

                if (solution.isAcceptedAnswer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Doğru Çözüm", tint = AccentTeal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Doğru Çözüm", color = AccentTeal, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                if (solution.authorId != currentUserId && currentUserId.isNotEmpty()) {
                    IconButton(
                        onClick = onReportClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Bildir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = solution.contentText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

