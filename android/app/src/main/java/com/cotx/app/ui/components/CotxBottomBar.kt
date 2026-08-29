package com.cotx.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cotx.app.ui.theme.PrimaryPurple
import com.cotx.app.ui.theme.SecondaryOrange

enum class CotxBottomTab {
    EXPLORE,
    HOME,
    MESSAGES,
    NOTIFICATIONS,
    PROFILE
}

@Composable
fun CotxBottomBar(
    currentTab: CotxBottomTab,
    unreadNotificationCount: Int = 0,
    unreadMessageCount: Int = 0,
    onTabSelected: (CotxBottomTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // 1. En Sol: Anasayfa (Takip Edilenler)
        NavigationBarItem(
            selected = currentTab == CotxBottomTab.HOME,
            onClick = { onTabSelected(CotxBottomTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Anasayfa") },
            label = { Text("Anasayfa") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                indicatorColor = PrimaryPurple.copy(alpha = 0.15f)
            )
        )

        // 2. Keşfet (Tüm öğrenciler)
        NavigationBarItem(
            selected = currentTab == CotxBottomTab.EXPLORE,
            onClick = { onTabSelected(CotxBottomTab.EXPLORE) },
            icon = { Icon(Icons.Default.Explore, contentDescription = "Keşfet") },
            label = { Text("Keşfet") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                indicatorColor = PrimaryPurple.copy(alpha = 0.15f)
            )
        )

        // 3. Mesajlar
        NavigationBarItem(
            selected = currentTab == CotxBottomTab.MESSAGES,
            onClick = { onTabSelected(CotxBottomTab.MESSAGES) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadMessageCount > 0) {
                            Badge(containerColor = SecondaryOrange) {
                                Text("$unreadMessageCount", color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Mesajlar")
                }
            },
            label = { Text("Mesajlar") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                indicatorColor = PrimaryPurple.copy(alpha = 0.15f)
            )
        )

        // 4. Bildirimler
        NavigationBarItem(
            selected = currentTab == CotxBottomTab.NOTIFICATIONS,
            onClick = { onTabSelected(CotxBottomTab.NOTIFICATIONS) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge(containerColor = SecondaryOrange) {
                                Text("$unreadNotificationCount", color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Bildirimler")
                }
            },
            label = { Text("Bildirimler") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                indicatorColor = PrimaryPurple.copy(alpha = 0.15f)
            )
        )

        // 5. En Sağ: Profil
        NavigationBarItem(
            selected = currentTab == CotxBottomTab.PROFILE,
            onClick = { onTabSelected(CotxBottomTab.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                indicatorColor = PrimaryPurple.copy(alpha = 0.15f)
            )
        )
    }
}
