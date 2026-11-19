package io.neta.music.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 圆形反色返回按钮（原播放页的样式）：比裸箭头显眼，横竖屏都好按。
 * 全应用的页面返回统一用它；[size] 跟随所在顶栏高度，播放页 64dp、普通顶栏 48dp、胶囊内 40dp。
 */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier, label: String = "返回", size: Dp = 64.dp) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(size).tvFocusRing(CircleShape),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        // 图标占按钮的 36/64，跟原播放页返回的比例一致
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, label, Modifier.size(size * 0.5625f))
    }
}
