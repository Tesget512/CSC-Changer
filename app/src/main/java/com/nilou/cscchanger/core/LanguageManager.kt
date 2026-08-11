package com.nilou.cscchanger.core

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.LocaleList
import android.os.Build
import java.util.Locale

/**
 * App 内语言切换（弥补地球图标）。
 * 在中文 / English 之间切换，需重建 Activity 生效。
 */
object LanguageManager {

    private const val PREFS = "lang_prefs"
    private const val KEY_LANG = "locale"

    /** 当前语言代码（zh / en），默认跟随系统（返回 null 表示系统默认） */
    fun current(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_LANG, null)
        return stored
    }

    /** 切换语言，返回切换后的代码 */
    fun toggle(context: Context): String {
        val next = if (current(context) == "zh") "en" else "zh"
        set(context, next)
        return next
    }

    fun set(context: Context, lang: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    /** 应用 Locale，返回配置后的 context */
    fun apply(context: Context): Context {
        val lang = current(context) ?: return context
        val locale = if (lang == "zh") Locale.SIMPLIFIED_CHINESE else Locale.ENGLISH
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= 24) {
            config.setLocales(LocaleList(locale))
        }
        return context.createConfigurationContext(config)
    }

    /** 判断当前是否为英文 */
    fun isEnglish(context: Context): Boolean = current(context) == "en"
}
