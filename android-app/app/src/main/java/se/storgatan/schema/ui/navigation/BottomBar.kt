package se.storgatan.schema.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import se.storgatan.schema.ui.theme.Accent
import se.storgatan.schema.ui.theme.AccentLight
import se.storgatan.schema.ui.theme.Background
import se.storgatan.schema.ui.theme.TextMuted

enum class BottomTab(val label: String, val icon: ImageVector, val route: Any) {
    MY_SHIFTS("Mina pass", Icons.Outlined.CalendarMonth, MyShiftsRoute),
    AVAILABLE("Lediga pass", Icons.Outlined.SwapHoriz, AvailableShiftsRoute),
    NOTIFICATIONS("Notiser", Icons.Outlined.Notifications, NotificationsRoute),
}

@Composable
fun SchemaBottomBar(
    currentTab: BottomTab?,
    unreadCount: Int,
    onTabSelected: (BottomTab) -> Unit,
) {
    NavigationBar(containerColor = Background) {
        BottomTab.entries.forEach { tab ->
            val selected = tab == currentTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == BottomTab.NOTIFICATIONS && unreadCount > 0) {
                        BadgedBox(badge = {
                            Badge(containerColor = Accent) {
                                Text(
                                    if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }) {
                            Icon(tab.icon, contentDescription = tab.label)
                        }
                    } else {
                        Icon(tab.icon, contentDescription = tab.label)
                    }
                },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = Accent,
                    indicatorColor = AccentLight,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                ),
            )
        }
    }
}
