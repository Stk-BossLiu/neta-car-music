package io.neta.music.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.neta.music.ui.remote.ScanInputDialog
import kotlinx.coroutines.launch

/**
 * 设置输入框右侧的「粘贴 + 扫码」入口：粘贴读系统剪贴板当一次手动输入回填；
 * 扫码打开手机远控的输入页，手机提交后走同一个 [onValueChange] 回来，校验与手动输入完全一致。
 */
@Composable
fun ScanInputIcons(label: String, onValueChange: (String) -> Unit, enabled: Boolean = true) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var scanOpen by rememberSaveable { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = {
                scope.launch {
                    val text = runCatching {
                        clipboard.getClipEntry()?.clipData?.let { data ->
                            if (data.itemCount > 0) data.getItemAt(0).text?.toString() else null
                        }
                    }.getOrNull()
                    if (text.isNullOrEmpty()) {
                        Toast.makeText(context, "剪贴板是空的", Toast.LENGTH_SHORT).show()
                    } else {
                        onValueChange(text)
                    }
                }
            },
            enabled = enabled,
            modifier = Modifier.tvFocusRing(),
        ) {
            Icon(Icons.Outlined.ContentPaste, "粘贴")
        }
        TextButton(
            onClick = { scanOpen = true },
            enabled = enabled,
            modifier = Modifier.tvFocusRing(),
        ) {
            Icon(Icons.Outlined.QrCode2, null, Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("扫码")
        }
    }

    if (scanOpen) {
        ScanInputDialog(label = label, onText = onValueChange, onDismiss = { scanOpen = false })
    }
}
