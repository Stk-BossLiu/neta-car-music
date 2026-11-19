package io.neta.music.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CdnMirror(val name: String = "", val prefix: String)

/** cdn-providers.json 的 CDN 线路表：应用更新与内置洛雪音源下载共用。 */
object CdnMirrors {
    private val json = Json { ignoreUnknownKeys = true }

    fun load(context: Context): List<CdnMirror> =
        context.assets.open("cdn-providers.json").bufferedReader().use { reader ->
            json.decodeFromString<List<CdnMirror>>(reader.readText())
                .map {
                    val prefix = it.prefix.trim().trimEnd('/')
                    it.copy(prefix = if (prefix.isEmpty()) "" else "$prefix/")
                }
                .distinctBy { it.prefix }
        }

    fun url(mirror: CdnMirror, target: String): String = mirror.prefix + target
}
