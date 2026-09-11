package com.cotx.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cotx.app.data.model.ChatMessage
import com.cotx.app.data.repository.AuthRepository
import com.cotx.app.data.repository.ChatRepository
import com.cotx.app.domain.model.UserSummary
import com.cotx.app.ui.theme.PrimaryPurple
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageScreen(
    receiverId: String,
    receiverName: String,
    receiverPhotoUrl: String = "",
    currentUser: UserSummary,
    isBlockedByMe: Boolean,
    authRepository: AuthRepository = remember { AuthRepository() },
    chatRepository: ChatRepository = remember { ChatRepository() },
    onNavigateToReportUser: (targetUserId: String, targetUserName: String) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var hasBlockedMe by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showTopMenu by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val isChatBlocked = isBlockedByMe || hasBlockedMe

    val roomId = remember(currentUser.id, receiverId) {
        chatRepository.getRoomId(currentUser.id, receiverId)
    }

    LaunchedEffect(receiverId) {
        authRepository.getUserProfile(receiverId).onSuccess { userObj ->
            hasBlockedMe = userObj?.blockedUsers?.contains(currentUser.id) == true
        }
    }

    LaunchedEffect(roomId) {
        if (currentUser.id.isNotEmpty() && roomId.isNotEmpty()) {
            chatRepository.markConversationAsRead(currentUser.id, roomId)
        }
    }

    DisposableEffect(roomId) {
        val listener: ListenerRegistration = chatRepository.listenPrivateMessages(roomId) { list ->
            messages = list
            if (currentUser.id.isNotEmpty()) {
                coroutineScope.launch {
                    chatRepository.markConversationAsRead(currentUser.id, roomId)
                }
            }
        }
        onDispose {
            listener.remove()
        }
    }

    val sortedMessages = remember(messages) {
        messages.sortedBy { it.createdAt?.time ?: Long.MAX_VALUE }
    }

    LaunchedEffect(sortedMessages.size) {
        if (sortedMessages.isNotEmpty()) {
            listState.animateScrollToItem(sortedMessages.size * 2)
        }
    }

    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) }

    if (messageToDelete != null) {
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("Mesajı Sil 🗑️") },
            text = { Text("Bu mesajı silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val targetMsg = messageToDelete
                        messageToDelete = null
                        if (targetMsg != null) {
                            coroutineScope.launch {
                                chatRepository.deletePrivateMessage(roomId, targetMsg.id)
                            }
                        }
                    }
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(receiverName.ifEmpty { "Öğrenci" }, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showTopMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menü")
                        }
                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Kullanıcıyı Bildir 🚩", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showTopMenu = false
                                    onNavigateToReportUser(receiverId, receiverName)
                                }
                            )
                            if (!isBlockedByMe) {
                                DropdownMenuItem(
                                    text = { Text("Kullanıcıyı Engelle 🚫", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showTopMenu = false
                                        coroutineScope.launch {
                                            authRepository.blockUser(currentUser.id, receiverId).onSuccess {
                                                android.widget.Toast.makeText(context, "Kullanıcı engellendi.", android.widget.Toast.LENGTH_SHORT).show()
                                                onNavigateBack()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                if (isChatBlocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Engelleme nedeniyle bu sohbet kapalıdır.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { if (it.length <= 1000) messageText = it },
                                placeholder = { Text("Mesajını yaz...") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
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
                                        val txt = messageText
                                        messageText = ""
                                        showEmojiPicker = false
                                        coroutineScope.launch {
                                            chatRepository.sendPrivateMessage(
                                                senderId = currentUser.id,
                                                senderName = currentUser.displayName.ifEmpty { "Öğrenci" },
                                                senderPhotoUrl = currentUser.avatarUrl ?: "",
                                                receiverId = receiverId,
                                                receiverName = receiverName,
                                                receiverPhotoUrl = receiverPhotoUrl,
                                                text = txt
                                            )
                                        }
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryPurple)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Gönder", tint = Color.White)
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
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sortedMessages.forEachIndexed { index, msg ->
                val msgDate = msg.createdAt
                val prevDate = if (index > 0) sortedMessages[index - 1].createdAt else null
                val showHeader = msgDate != null && (prevDate == null || !isSameDay(msgDate, prevDate))

                if (showHeader && msgDate != null) {
                    item(key = "header_${msgDate.time}_$index") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = formatHeaderDate(msgDate),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item(key = msg.id) {
                    MessageBubble(
                        message = msg,
                        isMine = msg.senderId == currentUser.id,
                        onDelete = { messageToDelete = msg }
                    )
                }
            }
        }
    }
}

private fun isSameDay(date1: Date?, date2: Date?): Boolean {
    if (date1 == null || date2 == null) return false
    val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
    val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
    return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}

private fun formatHeaderDate(date: Date): String {
    val today = java.util.Calendar.getInstance()
    val msgCal = java.util.Calendar.getInstance().apply { time = date }
    val yesterday = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        msgCal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                msgCal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) -> "Bugün"

        msgCal.get(java.util.Calendar.YEAR) == yesterday.get(java.util.Calendar.YEAR) &&
                msgCal.get(java.util.Calendar.DAY_OF_YEAR) == yesterday.get(java.util.Calendar.DAY_OF_YEAR) -> "Dün"

        else -> java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("tr")).format(date)
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    onDelete: () -> Unit
) {
    val dateFormat = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale("tr")) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            color = if (isMine) PrimaryPurple else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clickable { showMenu = true }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                message.createdAt?.let { time ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateFormat.format(time),
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Mesajı Sil", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}
