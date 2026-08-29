package com.cotx.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cotx.app.ui.theme.PrimaryPurple

enum class EmojiCategory(val title: String, val icon: String, val emojis: List<String>) {
    POPULAR(
        title = "Popüler",
        icon = "🔥",
        emojis = listOf("👍", "❤️", "😂", "🔥", "👏", "🎉", "🙌", "💯", "🤔", "✨", "😍", "🥳", "💡", "🙏")
    ),
    EMOTIONS(
        title = "Duygular",
        icon = "😊",
        emojis = listOf("😊", "😂", "🤣", "😄", "😍", "🥰", "😉", "🥳", "😎", "🤩", "🥺", "😭", "😅", "🤯", "🥱", "🧐", "🙃", "😬", "😇", "😜", "😴", "🤫", "💪", "👊")
    ),
    STUDY(
        title = "Ders & Okul",
        icon = "📚",
        emojis = listOf("📚", "📖", "✍️", "📝", "🎓", "🏫", "📐", "📏", "🔬", "🧪", "💻", "🎒", "🧠", "🎯", "📊", "💡", "❓", "⚡", "✏️", "🏆", "⏳", "📌")
    ),
    SYMBOLS(
        title = "Semboller",
        icon = "❤️",
        emojis = listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "💯", "🔥", "⭐", "🌟", "✨", "🎉", "🎁", "🎈", "📢", "🚀", "🎨", "⚽", "🏀", "🎮")
    )
}

@Composable
fun EmojiPickerPanel(
    onEmojiSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(EmojiCategory.POPULAR) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category Tabs
            TabRow(
                selectedTabIndex = selectedCategory.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryPurple,
                divider = {}
            ) {
                EmojiCategory.values().forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(
                                text = "${category.icon} ${category.title}",
                                fontSize = 11.sp,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Emoji Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(selectedCategory.emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onEmojiSelect(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }
    }
}
