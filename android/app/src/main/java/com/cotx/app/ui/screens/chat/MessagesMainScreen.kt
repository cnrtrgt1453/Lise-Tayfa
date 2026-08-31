package com.cotx.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
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
import com.cotx.app.data.model.ChatConversation
import com.cotx.app.data.model.ChatMessage
import com.cotx.app.domain.model.UserSummary
import com.cotx.app.ui.components.CotxBottomBar
import com.cotx.app.ui.components.CotxBottomTab
import com.cotx.app.ui.theme.PrimaryPurple
import com.cotx.app.ui.theme.SecondaryOrange
import com.cotx.app.viewmodel.ChatViewModel
import com.cotx.app.viewmodel.MessagesTab
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesMainScreen(
    viewModel: ChatViewModel,
    currentUser: UserSummary,
    unreadNotificationCount: Int = 0,
    unreadMessageCount: Int = 0,
    onNavigateToPrivateDM: (receiverId: String, receiverName: String) -> Unit,
    onNavigateToExplore: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: (userId: String?) -> Unit = {}
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val globalMessages by viewModel.globalMessages.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(currentUser.id) {
        if (currentUser.id.isNotEmpty()) {
            viewModel.initListeners(currentUser.id)
            viewModel.markAllConversationsAsRead(currentUser.id)
        }
    }

    LaunchedEffect(globalMessages.size) {
        if (globalMessages.isNotEmpty()) {
            listState.animateScrollToItem(globalMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mesajlar 💬",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            CotxBottomBar(
                currentTab = CotxBottomTab.MESSAGES,
                unreadNotificationCount = unreadNotificationCount,
                unreadMessageCount = unreadMessageCount,
                onTabSelected = { tab ->
                    when (tab) {
                        CotxBottomTab.EXPLORE -> onNavigateToExplore()
                        CotxBottomTab.HOME -> onNavigateToHome()
                        CotxBottomTab.MESSAGES -> {}
                        CotxBottomTab.NOTIFICATIONS -> onNavigateToNotifications()
                        CotxBottomTab.PROFILE -> onNavigateToProfile(null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Row (Global Sohbet vs Özel Mesajlar)
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryPurple,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
            ) {
                Tab(
                    selected = selectedTab == MessagesTab.GLOBAL_CHAT,
                    onClick = { viewModel.selectTab(MessagesTab.GLOBAL_CHAT) },
                    text = {
                        Text(
                            "🌐 Global Sohbet",
                            fontWeight = if (selectedTab == MessagesTab.GLOBAL_CHAT) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == MessagesTab.PRIVATE_MESSAGES,
                    onClick = { viewModel.selectTab(MessagesTab.PRIVATE_MESSAGES) },
                    text = {
                        Text(
                            "🔒 Özel Mesajlar",
                            fontWeight = if (selectedTab == MessagesTab.PRIVATE_MESSAGES) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Tab Content
            when (selectedTab) {
                MessagesTab.GLOBAL_CHAT -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Info Header
                        Surface(
                            color = PrimaryPurple.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ortak sohbet kanalıdır. Profillere gitmek için isim veya fotoğraflara tıklayabilirsiniz.",
                                    style = MaterialTheme.typography.labelSmall.copy(color = PrimaryPurple)
                                )
                            }
                        }

                        // Messages List
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(globalMessages) { msg ->
                                GlobalMessageItem(
                                    message = msg,
                                    isMe = msg.senderId == currentUser.id,
                                    onAuthorClick = { userId -> onNavigateToProfile(userId) }
                                )
                            }
                        }

                        // Input Box
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = messageText,
                                        onValueChange = { messageText = it },
                                        placeholder = { Text("Global sohbete yaz...") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(24.dp),
                                        trailingIcon = {
                                            IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                                                Text("😊", fontSize = 20.sp)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (messageText.isNotBlank()) {
                                                viewModel.sendGlobalMessage(currentUser, messageText)
                                                messageText = ""
                                                showEmojiPicker = false
                                            }
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryPurple)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Gönder", tint = Color.White)
                                    }
                                }

                                if (showEmojiPicker) {
                                    com.cotx.app.ui.components.EmojiPickerPanel(
                                        onEmojiSelect = { emoji ->
                                            messageText += emoji
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                MessagesTab.PRIVATE_MESSAGES -> {
                    if (conversations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Henüz özel mesajınız yok",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Öğrencilerin profil sayfasına giderek 'Mesaj Gönder' butonuna basabilir ve birebir özel sohbet başlatabilirsiniz.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        var convToDelete by remember { mutableStateOf<ChatConversation?>(null) }

                        if (convToDelete != null) {
                            AlertDialog(
                                onDismissRequest = { convToDelete = null },
                                title = { Text("Sohbeti Sil 🗑️") },
                                text = { Text("${convToDelete?.otherUserName} ile olan sohbeti listenizden silmek istediğinize emin misiniz?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val target = convToDelete
                                            convToDelete = null
                                            if (target != null && currentUser.id.isNotEmpty()) {
                                                viewModel.deleteConversation(currentUser.id, target.chatRoomId)
                                            }
                                        }
                                    ) {
                                        Text("Sil", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { convToDelete = null }) {
                                        Text("İptal")
                                    }
                                }
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(conversations) { conv ->
                                ConversationItem(
                                    conversation = conv,
                                    onClick = {
                                        onNavigateToPrivateDM(conv.otherUserId, conv.otherUserName)
                                    },
                                    onDelete = {
                                        convToDelete = conv
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
fun GlobalMessageItem(
    message: ChatMessage,
    isMe: Boolean,
    onAuthorClick: (userId: String) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f))
                    .clickable { onAuthorClick(message.senderId) },
                contentAlignment = Alignment.Center
            ) {
                if (message.senderPhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = com.cotx.app.util.ImageModelResolver.resolve(message.senderPhotoUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = message.senderName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (!isMe) {
                Text(
                    text = message.senderName.ifEmpty { "Öğrenci" },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryPurple),
                    modifier = Modifier.clickable { onAuthorClick(message.senderId) }
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                color = if (isMe) PrimaryPurple else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ChatConversation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (conversation.otherUserPhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = com.cotx.app.util.ImageModelResolver.resolve(conversation.otherUserPhotoUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = conversation.otherUserName.ifEmpty { "Ö" }.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.otherUserName.ifEmpty { "Öğrenci" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            conversation.lastMessageTime?.let { date ->
                Spacer(modifier = Modifier.width(8.dp))
                val dateFormat = SimpleDateFormat("HH:mm", Locale("tr"))
                Text(
                    text = dateFormat.format(date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sohbeti Sil",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
