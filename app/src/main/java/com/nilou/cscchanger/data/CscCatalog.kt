package com.nilou.cscchanger.data

import android.content.Context
import org.json.JSONArray

/** 单个 CSC 条目 */
data class CscEntry(
    val code: String,
    val region: String,
) {
    val countryCode: String?
        get() = com.nilou.cscchanger.core.CscConstants.countryCodeOf(code)
}

/**
 * CSC 目录：从 assets/csc_list.json 加载（467 个代码），进程内缓存。
 * 数据来源：research/samsung-csc-codes（scripts/parse_csc_list.py 生成）。
 */
object CscCatalog {

    @Volatile
    private var cached: List<CscEntry>? = null

    fun load(context: Context): List<CscEntry> {
        cached?.let { return it }
        val text = context.assets.open(CSC_LIST_ASSET).bufferedReader().use { it.readText() }
        val array = JSONArray(text)
        val list = (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            CscEntry(
                code = obj.getString("code"),
                region = obj.getString("region"),
            )
        }
        cached = list
        return list
    }

    /** 搜索：按代码前缀或地区名（忽略大小写） */
    fun search(list: List<CscEntry>, query: String): List<CscEntry> {
        val q = query.trim()
        if (q.isEmpty()) return list
        return list.filter {
            it.code.contains(q, ignoreCase = true) ||
                it.region.contains(q, ignoreCase = true)
        }
    }

    private const val CSC_LIST_ASSET = "csc_list.json"
}
