package com.cotx.app.ui.screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonAdd
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
import com.cotx.app.data.model.Notification
import com.cotx.app.ui.components.CotxBottomBar
import com.cotx.app.ui.components.CotxBottomTab
import com.cotx.app.ui.theme.PrimaryPurple
import com.cotx.app.ui.theme.SecondaryOrange
import com.cotx.app.viewmodel.NotificationUiState
import com.cotx.app.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    currentUserId: String,
    unreadNotificationCount: Int = 0,
    unreadMessageCount: Int = 0,
    onNavigateToQuestionDetail: (questionId: String) -> Unit,
    onNavigateToExplore: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToDMList: () -> Unit = {},
    onNavigateToProfile: (userId: String?) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.loadNotifications(currentUserId)
            viewModel.markAllAsRead(currentUserId)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bildirimler 🔔", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        bottomBar = {
            CotxBottomBar(
                currentTab = CotxBottomTab.NOTIFICATIONS,
                unreadNotificationCount = unreadNotificationCount,
                unreadMessageCount = unreadMessageCount,
                onTabSelected = { tab ->
                    when (tab) {
                        CotxBottomTab.EXPLORE -> onNavigateToExplore()
                        CotxBottomTab.HOME -> onNavigateToHome()
                        CotxBottomTab.MESSAGES -> onNavigateToDMList()
                        CotxBottomTab.NOTIFICATIONS -> {}
                        CotxBottomTab.PROFILE -> onNavigateToProfile(null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is NotificationUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryPurple
                    )
                }
                is NotificationUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadNotifications(currentUserId) }) {
                            Text("Tekrar Dene")
                        }
                    }
                }
                is NotificationUiState.Success -> {
                    if (state.notifications.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Henüz bildiriminiz yok",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sorularına gelen beğeniler ve çözümler burada listelenecektir.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.notifications) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onClick = {
                                        viewModel.markAsRead(notification.id, currentUserId)
                                        if (notification.type == "FOLLOW" || (notification.questionId.isEmpty() && notification.senderId.isNotEmpty())) {
                                            if (notification.senderId.isNotEmpty()) {
                                                onNavigateToProfile(notification.senderId)
                                            }
                                        } else if (notification.questionId.isNotEmpty()) {
                                            onNavigateToQuestionDetail(notification.questionId)
                                        }
                                    },
                                    onSenderClick = { senderId ->
                                        if (senderId.isNotEmpty()) {
                                            onNavigateToProfile(senderId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onSenderClick: (senderId: String) -> Unit = {}
) {
    val bgColor = if (!notification.isRead) PrimaryPurple.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 2.dp else 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sender Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f))
                    .clickable { onSenderClick(notification.senderId) },
                contentAlignment = Alignment.Center
            ) {
                if (notification.senderPhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = com.cotx.app.util.ImageModelResolver.resolve(notification.senderPhotoUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = notification.senderName.ifEmpty { "Ö" }.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Notification Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                notification.createdAt?.let { date ->
                    val dateFormat = SimpleDateFormat("HH:mm - dd MMM", Locale("tr"))
                    Text(
                        text = dateFormat.format(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Icon indicator
            when (notification.type) {
                "LIKE" -> Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                "FOLLOW" -> Icon(Icons.Default.PersonAdd, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                "NEW_QUESTION" -> Icon(Icons.Default.ChatBubble, contentDescription = null, tint = SecondaryOrange, modifier = Modifier.size(20.dp))
                else -> Icon(Icons.Default.ChatBubble, contentDescription = null, tint = SecondaryOrange, modifier = Modifier.size(20.dp))
            }

            // Unread Badge Dot
            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                )
            }
        }
    }
}
