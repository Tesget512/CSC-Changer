package com.nilou.cscchanger

import android.app.Application
import android.util.Log
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

/**
 * 模块 UI 进程 Application：
 * 绑定 LSPosed 的 XposedService，用于把配置写入框架专用 remote prefs 存储
 * （与 hook 侧 [io.github.libxposed.api.XposedInterface.getRemotePreferences]
 * 读取的是同一份，避免 SELinux 的 app_data_file 隔离导致跨进程读不到）。
 */
class App : Application(), XposedServiceHelper.OnServiceListener {

    @Volatile
    private var mService: XposedService? = null

    companion object {
        private const val TAG = "CscChanger"

        @Volatile
        private var service: XposedService? = null

        /** 供 CscPrefs 等静态工具访问已绑定的 service（可能为 null，若框架未连接） */
        @JvmStatic
        fun getXposedService(): XposedService? = service
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "App.onCreate: registering XposedService listener")
        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(service: XposedService) {
        Log.i(TAG, "App.onServiceBind: XposedService connected, apiVersion=${service.apiVersion} framework=${service.frameworkName}")
        synchronized(this) {
            mService = service
            Companion.service = service
        }
    }

    override fun onServiceDied(service: XposedService) {
        Log.w(TAG, "App.onServiceDied: XposedService died")
        synchronized(this) {
            if (mService === service) {
                mService = null
                Companion.service = null
            }
        }
    }
}
