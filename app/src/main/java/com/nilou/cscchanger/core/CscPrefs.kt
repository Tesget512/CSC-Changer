package com.nilou.cscchanger.core

import android.content.Context
import android.content.SharedPreferences
import com.nilou.cscchanger.App

/**
 * 配置读写（libxposed 102 API 方案）：
 *
 * 存储走 **LSPosed 框架的 remote prefs**（`XposedService.getRemotePreferences`），
 * 与 hook 侧 `XposedInterface.getRemotePreferences` 读取的是同一份存储，
 * 由框架服务以 root 转发，天然跨进程、无 SELinux app_data_file 隔离问题。
 *
 * 注意：不要用 `Context.getSharedPreferences` 写配置——那会落到模块私有目录
 * （shared_prefs/），宿主进程（Galaxy Store 等）因 SELinux 隔离读不到。
 *
 * UI 侧读取逻辑：
 * 1. 优先走 XposedService remote prefs（若框架已连接）
 * 2. fallback 普通 SharedPreferences（框架未连接时 UI 仍可独立读写）
 */
object CscPrefs {

    /**
     * 写入配置。优先写入框架 remote prefs（hook 侧可读到），
     * 框架未连接时 fallback 到本地普通 prefs。
     */
    fun save(context: Context, config: CscConfig) {
        val remote = App.getXposedService()?.getRemotePreferences(CscConstants.PREFS_NAME)
        if (remote != null) {
            val editor = remote.edit()
            CscConfig.toPrefs(editor, config)
            editor.apply()
            // 双写本地，保证 UI 进程自己也能读到（框架掉线/重启后仍有值）
            val localEditor = localPrefs(context).edit()
            CscConfig.toPrefs(localEditor, config)
            localEditor.apply()
        } else {
            val editor = localPrefs(context).edit()
            CscConfig.toPrefs(editor, config)
            editor.apply()
        }
    }

    /** UI 侧读取：优先框架 remote prefs，fallback 本地 */
    fun load(context: Context): CscConfig {
        val remote = App.getXposedService()?.getRemotePreferences(CscConstants.PREFS_NAME)
        if (remote != null && !remote.all.isEmpty()) {
            return CscConfig.fromPrefs(remote)
        }
        return CscConfig.fromPrefs(localPrefs(context))
    }

    /** UI 侧重置 */
    fun clear(context: Context) {
        App.getXposedService()?.getRemotePreferences(CscConstants.PREFS_NAME)?.edit()?.clear()?.apply()
        localPrefs(context).edit().clear().apply()
    }

    /** 本地普通 prefs（仅 UI 进程自用/fallback） */
    private fun localPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(CscConstants.PREFS_NAME, Context.MODE_PRIVATE)
}
