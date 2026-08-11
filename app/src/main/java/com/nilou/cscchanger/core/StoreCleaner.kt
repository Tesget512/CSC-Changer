package com.nilou.cscchanger.core

/**
 * Galaxy Store 数据清理工具。
 * 清除后商店会重新判定区域（读取被 hook 的 SIM/CSC），使伪装生效。
 */
object StoreCleaner {

    /** Galaxy Store 包名 */
    const val GALAXY_STORE = "com.sec.android.app.samsungapps"
    const val THEME_STORE = "com.samsung.android.themestore"

    /** root 一键清理 Galaxy Store 数据，返回成功与否 */
    fun clearStoreDataRoot(): Boolean {
        if (!RootShell.hasRoot()) return false
        val (code, _) = RootShell.exec("pm clear $GALAXY_STORE")
        return code == 0
    }

    /** 跳转系统设置的应用信息页（无需 root） */
    fun buildAppInfoIntent(packageName: String): android.content.Intent =
        android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            android.net.Uri.fromParts("package", packageName, null),
        ).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    /** 跳转 Galaxy Store 应用信息页 */
    fun buildStoreAppInfoIntent(): android.content.Intent =
        buildAppInfoIntent(GALAXY_STORE)
}
