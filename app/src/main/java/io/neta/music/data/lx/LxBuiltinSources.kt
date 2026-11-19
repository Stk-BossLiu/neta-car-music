package io.neta.music.data.lx

/** 内置洛雪音源目录项：name 只用于下载前的列表展示，导入后仍以脚本 @name 头为准。 */
data class LxBuiltinSource(val name: String, val url: String)

/** 一次内置批量导入的结果：existing 为已存在跳过，skipped 为数量已满跳过，failed 为下载或校验失败。 */
data class BuiltinImportSummary(val imported: Int, val existing: Int, val skipped: Int, val failed: Int)

/** pdone 合集中的内置音源，按目录顺序即默认尝试顺序；只存规范 raw 地址，下载时套 CDN 线路。 */
object LxBuiltinSources {
    private const val BASE = "https://raw.githubusercontent.com/pdone/lx-music-source/main"

    val all: List<LxBuiltinSource> = listOf(
        LxBuiltinSource("Huibq", "$BASE/huibq/latest.js"),
        LxBuiltinSource("Flower", "$BASE/flower/latest.js"),
        LxBuiltinSource("ikun", "$BASE/ikun/latest.js"),
        LxBuiltinSource("Grass", "$BASE/grass/latest.js"),
        LxBuiltinSource("JuheApi", "$BASE/juhe/latest.js"),
        LxBuiltinSource("QDY", "$BASE/qdy/latest.js"),
    )
}
