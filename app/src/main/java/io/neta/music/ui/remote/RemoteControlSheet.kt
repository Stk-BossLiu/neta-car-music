package io.neta.music.ui.remote

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import io.neta.music.MyMusicApp
import io.neta.music.ui.components.AdaptiveSheet
import io.neta.music.ui.settings.QrCodeImage
import java.net.URLEncoder

/** 主界面右上角二维码打开的面板：服务状态、局域网地址二维码和启停按钮。 */
@Composable
fun RemoteControlSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as MyMusicApp }
    val manager = remember(app) { app.remoteControl }
    val state by manager.state.collectAsState()
    // 打开面板、每次启动/重启（running 翻转）都重新解析当前局域网地址
    var address by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.running) {
        address = if (state.running) manager.address() else null
    }

    AdaptiveSheet(onDismiss = onDismiss, title = "手机远控") {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                when {
                    state.serviceError.isNotBlank() -> "启动失败：${state.serviceError}"
                    state.running -> "运行中 · 手机扫码即用"
                    !state.enabled -> "已禁用 · 不随应用自动启动"
                    else -> "已停止"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.running) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            if (state.running) {
                if (address == null) {
                    Text(
                        "未找到局域网地址，请确认车机连着 Wi-Fi 或热点，再点「重启」重试。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(224.dp),
                    ) {
                        QrCodeImage(address.orEmpty(), Modifier.fillMaxWidth().height(224.dp), description = "远控网页二维码")
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            context.getSystemService(android.content.ClipboardManager::class.java)
                                ?.setPrimaryClip(ClipData.newPlainText("手机远控地址", address))
                            Toast.makeText(context, "地址已复制", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(address.orEmpty())
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
            Text(
                "手机和车机连接同一个 Wi-Fi 或热点，扫码打开网页：搜歌点歌、双向传剪贴板；" +
                    "设置里的输入框旁也有扫码入口，手机上打字填进来。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            // 按钮整行铺满、等宽均分（和加歌单面板等弹窗同一套排法）
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                when {
                    state.running -> {
                        Button(onClick = manager::restart, modifier = Modifier.weight(1f)) { Text("重启") }
                        OutlinedButton(onClick = manager::stop, modifier = Modifier.weight(1f)) { Text("停止") }
                        OutlinedButton(onClick = { manager.setEnabled(false) }, modifier = Modifier.weight(1f)) { Text("禁用") }
                    }
                    state.enabled -> {
                        Button(onClick = manager::start, modifier = Modifier.weight(1f)) { Text("启动") }
                        OutlinedButton(onClick = { manager.setEnabled(false) }, modifier = Modifier.weight(1f)) { Text("禁用") }
                    }
                    else -> Button(onClick = { manager.setEnabled(true) }, modifier = Modifier.weight(1f)) { Text("启用") }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * 输入框旁「扫码」打开的对话框：展示输入页二维码，手机提交后文本经 [onText] 回填。
 * 服务没在跑就顺手拉起；已禁用则提示先去主界面启用。地址每次打开重新解析。
 */
@Composable
fun ScanInputDialog(label: String, onText: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as MyMusicApp }
    val manager = remember(app) { app.remoteControl }
    val state by manager.state.collectAsState()
    var address by remember { mutableStateOf<String?>(null) }
    val latestOnText by rememberUpdatedState(onText)
    val latestDismiss by rememberUpdatedState(onDismiss)

    LaunchedEffect(Unit) {
        if (!state.running && state.enabled) manager.start()
    }
    LaunchedEffect(state.running) {
        address = if (state.running) manager.address() else null
    }
    DisposableEffect(label) {
        manager.registerInput(label) { text ->
            latestOnText(text)
            latestDismiss()
        }
        onDispose { manager.clearInput() }
    }

    val disabled = !state.enabled && !state.running
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("扫码输入 · $label") },
        text = {
            when {
                disabled -> Text("手机远控已禁用，请到主界面右上角的二维码里启用后再试。")
                !state.running -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(10.dp))
                    Text("正在启动远控服务…")
                }
                address == null -> Text("未找到局域网地址，请确认车机连着 Wi-Fi 或热点后重试。")
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.size(220.dp)) {
                        QrCodeImage(
                            address.orEmpty() + "/input?field=" + URLEncoder.encode(label, "UTF-8"),
                            Modifier.size(220.dp),
                            description = "扫码输入二维码",
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(address.orEmpty(), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "手机和车机连同一 Wi-Fi，扫码输入后发送，这里会自动填好并关闭。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )
}
