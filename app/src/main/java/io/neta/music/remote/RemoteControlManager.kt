package io.neta.music.remote

import android.content.Intent
import androidx.core.content.ContextCompat
import io.neta.music.MyMusicApp
import io.neta.music.data.sync.findSyncSetupAddress
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal data class RemoteControlState(
    /** 随应用自动启动的开关；关掉（禁用）后不再自动拉起，只能手动启动。 */
    val enabled: Boolean = true,
    val running: Boolean = false,
    val serviceError: String = "",
)

/** 扫码输入的待填槽位：网页提交的文本经 [onText] 在主线程回填到发起扫码的输入框。同一时间只有一个。 */
internal class RemotePendingInput(val label: String, val onText: (String) -> Unit)

/**
 * 手机远控：局域网网页（搜歌点歌、剪贴板传输、扫码输入）的启停与状态。
 * 生命周期照抄 MCP：[RemoteControlService] 前台服务持有 HTTP 服务，这里只发指令、记状态、存开关。
 */
internal class RemoteControlManager(private val app: MyMusicApp) {
    private val mutableState = MutableStateFlow(RemoteControlState())
    val state = mutableState.asStateFlow()
    private val loaded = app.appScope.async {
        val saved = app.settings.state.first()
        mutableState.update { it.copy(enabled = saved.remoteEnabled) }
    }

    @Volatile
    private var pending: RemotePendingInput? = null

    /** 当前手机可访问的局域网地址；每次都重新枚举网卡，Wi-Fi / 热点优先。 */
    fun address(): String? {
        val host = findSyncSetupAddress()?.hostAddress ?: return null
        return "http://$host:$PORT"
    }

    fun start() {
        ContextCompat.startForegroundService(app, Intent(app, RemoteControlService::class.java))
    }

    fun stop() {
        app.stopService(Intent(app, RemoteControlService::class.java))
    }

    /** 网页点「重启」：停掉再拉起，地址跟着重新解析。 */
    fun restart() = app.appScope.launch {
        loaded.await()
        if (state.value.running) {
            stop()
            withTimeoutOrNull(5_000) { state.first { !it.running } }
        }
        start()
    }

    /** 禁用 = 关掉并不再自动启动；启用 = 记开关并立刻拉起。 */
    fun setEnabled(enabled: Boolean) = app.appScope.launch {
        loaded.await()
        app.settings.remoteEnabled(enabled)
        mutableState.update { it.copy(enabled = enabled, serviceError = "") }
        if (enabled) start() else stop()
    }

    suspend fun restoreIfEnabled() {
        loaded.await()
        if (state.value.enabled && !state.value.running) start()
    }

    fun serviceStarted() = mutableState.update { it.copy(running = true, serviceError = "") }

    fun serviceStopped(error: String = "") = mutableState.update { it.copy(running = false, serviceError = error) }

    fun registerInput(label: String, onText: (String) -> Unit) {
        pending = RemotePendingInput(label, onText)
    }

    fun clearInput() {
        pending = null
    }

    fun pendingInput(): RemotePendingInput? = pending

    companion object {
        const val PORT = 18790
    }
}
