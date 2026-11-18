package io.neta.music.ui.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Minimize
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.neta.music.ui.TopTab
import io.neta.music.ui.components.LocalAppWindow
import io.neta.music.ui.components.tvFocusRing

/** 手机、平板与车机共用顶部导航；Tab 可横向滚动。大屏右侧依次是离开、退出和设置，手机收进「更多」菜单。 */
@Composable
fun HomeTopBar(
    selected: String,
    onSelect: (String) -> Unit,
    onSettings: () -> Unit,
    onLeaveApp: () -> Unit,
    onExitApp: () -> Unit,
) {
    val window = LocalAppWindow.current
    val tabs = TopTab.entries
    val selectedIndex = tabs.indexOfFirst { it.route == selected }.coerceAtLeast(0)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = window.gutter, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 窗口变窄后重新定位当前 Tab，避免选中的入口留在视口外。
        key(window.widthDp) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedIndex,
                modifier = Modifier.weight(1f).semantics { contentDescription = "首页导航" },
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 0.dp,
                divider = {},
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedIndex,
                        onClick = { onSelect(tab.route) },
                        modifier = Modifier.heightIn(min = 72.dp).tvFocusRing(),
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                tab.label,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                            )
                        },
                    )
                }
            }
        }
        if (window.isPhone) {
            // 窄屏顶栏塞不下三个按钮：收进「更多」，点开再选
            var menuOpen by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.size(48.dp).tvFocusRing(CircleShape),
                ) {
                    Icon(Icons.Outlined.MoreVert, "更多", Modifier.size(26.dp))
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    shape = RoundedCornerShape(20.dp),
                ) {
                    DropdownMenuItem(
                        text = { Text("离开应用") },
                        leadingIcon = { Icon(Icons.Outlined.Minimize, null) },
                        onClick = { menuOpen = false; onLeaveApp() },
                    )
                    DropdownMenuItem(
                        text = { Text("退出应用") },
                        leadingIcon = { Icon(Icons.Outlined.PowerSettingsNew, null) },
                        onClick = { menuOpen = false; onExitApp() },
                    )
                    DropdownMenuItem(
                        text = { Text("设置") },
                        leadingIcon = { Icon(Icons.Outlined.Settings, null) },
                        onClick = { menuOpen = false; onSettings() },
                    )
                }
            }
        } else {
            IconButton(
                onClick = onLeaveApp,
                modifier = Modifier.size(48.dp).tvFocusRing(CircleShape),
            ) {
                Icon(Icons.Outlined.Minimize, "离开应用", Modifier.size(26.dp))
            }
            IconButton(
                onClick = onExitApp,
                modifier = Modifier.size(48.dp).tvFocusRing(CircleShape),
            ) {
                Icon(Icons.Outlined.PowerSettingsNew, "退出应用", Modifier.size(26.dp))
            }
            IconButton(
                onClick = onSettings,
                modifier = Modifier.size(48.dp).tvFocusRing(CircleShape),
            ) {
                Icon(Icons.Outlined.Settings, "设置", Modifier.size(26.dp))
            }
        }
    }
}
