package io.neta.music.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import io.neta.music.MainActivity
import io.neta.music.MyMusicApp
import io.neta.music.R
import io.neta.music.buildAppNotification

/**
 * 手机远控的前台服务：持有局域网 HTTP 服务，没在播放时也保持可用。
 * 生命周期与 MCP 服务一致，独立渠道和通知，互不影响。
 */
class RemoteControlService : Service() {
    private var server: RemoteControlServer? = null

    override fun onCreate() {
        super.onCreate()
        val app = application as MyMusicApp
        try {
            val manager = getSystemService(NotificationManager::class.java)
            if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(
                NotificationChannel(CHANNEL, getString(R.string.remote_channel), NotificationManager.IMPORTANCE_LOW))
            val open = PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_mymusic)
                .setContentTitle(getString(R.string.remote_notification_title))
                .setContentText(getString(R.string.remote_notification_text))
                .setContentIntent(open).setOngoing(true).setSilent(true)
                .buildAppNotification()
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification,
                if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0)
            val created = RemoteControlServer(app)
            server = created
            created.start(60_000, false)
            app.remoteControl.serviceStarted()
        } catch (exception: Exception) {
            server?.stop()
            server = null
            app.remoteControl.serviceStopped(exception.message ?: "手机远控服务启动失败")
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        if (server != null) START_STICKY else START_NOT_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        server?.stop()
        server = null
        val manager = (application as MyMusicApp).remoteControl
        // 保留启动失败的具体原因，正常停止时只更新运行状态。
        manager.serviceStopped(manager.state.value.serviceError)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private companion object {
        const val CHANNEL = "remote_control"
        const val NOTIFICATION_ID = RemoteControlManager.PORT
    }
}
