package com.nilou.cscchanger.core

/**
 * 内置机型伪装库：主流三星机型 -> 模拟用的 Build 属性。
 *
 * 型号/代号来源（仅保留已查证）：
 * - Galaxy Z Fold7 = SM-F966，代号 q7
 * - Galaxy Z Flip7 = SM-F766，代号 b6r
 * - Galaxy Z Fold6 = SM-F956，代号 q6
 * - Galaxy Z Flip6 = SM-F741，代号 b6q
 * - Galaxy Z Flip5 = SM-F731，代号 b5q（用户本机）
 * - Galaxy Z Fold5 = SM-F946，代号 q5
 * - Galaxy S26 Ultra = SM-S939，代号 a9e
 * - Galaxy S26 = SM-S936，代号 a9
 * - Galaxy S25 Ultra = SM-S938，代号 a9
 * - Galaxy S25 = SM-S936，代号 a9
 * - Galaxy S24 Ultra = SM-S928，代号 dm3q
 *
 * 移除（未查证/代号错）：
 * - Galaxy Z Fold8（代号实际为 h8q，不在本表，留待查证后追加）
 * - Galaxy Z Fold8 Ultra、Z Flip8（未确认）
 *
 * 说明：ro.product.model / ro.product.device / ro.build.product / ro.product.name
 * 是 Build 读取的主入口；model 显示为 "SM-F9760" 等（0 结尾为国行变体）。
 */
data class DeviceProfile(
    /** 展示名，如 "Galaxy Z Fold8" */
    val name: String,
    /** 型号，如 SM-F9760（0 结尾国行变体） */
    val model: String,
    /** 设备代号，如 q8 */
    val device: String,
    /** ro.product.name，如 q8zc */
    val productName: String,
    /** 系列名，如 "Z Fold8" 用于分组 */
    val family: String,
) {
    val displayName: String get() = "$name ($model)"
}

object DeviceModels {

    /** 所有内置机型（按家族分组展示） */
    val ALL: List<DeviceProfile> = listOf(
        // ==== Z Fold 系列 ====
        DeviceProfile("Galaxy Z Fold8", "SM-F971B", "h8q", "h8qxxx", "Z Fold"),
        DeviceProfile("Galaxy Z Fold7", "SM-F9660", "q7", "q7zc", "Z Fold"),
        DeviceProfile("Galaxy Z Fold6", "SM-F9560", "q6", "q6zc", "Z Fold"),
        DeviceProfile("Galaxy Z Fold5", "SM-F9460", "q5", "q5zc", "Z Fold"),
        // ==== Z Flip 系列 ====
        DeviceProfile("Galaxy Z Flip7", "SM-F7660", "b6r", "b6rzc", "Z Flip"),
        DeviceProfile("Galaxy Z Flip6", "SM-F7410", "b6q", "b6qzc", "Z Flip"),
        DeviceProfile("Galaxy Z Flip5", "SM-F7310", "b5q", "b5qzc", "Z Flip"),
        // ==== S 系列 ====
        DeviceProfile("Galaxy S26 Ultra", "SM-S9390", "a9e", "a9ezc", "Galaxy S"),
        DeviceProfile("Galaxy S26", "SM-S9360", "a9", "a9zc", "Galaxy S"),
        DeviceProfile("Galaxy S25 Ultra", "SM-S9380", "a9", "a9zc", "Galaxy S"),
        DeviceProfile("Galaxy S25", "SM-S9360", "a9", "a9zc", "Galaxy S"),
        DeviceProfile("Galaxy S24 Ultra", "SM-S9280", "dm3q", "dm3qzc", "Galaxy S"),
    )

    /** 按家族分组（保持顺序） */
    val BY_FAMILY: Map<String, List<DeviceProfile>> = ALL.groupBy { it.family }

    fun findByName(name: String): DeviceProfile? = ALL.find { it.name == name }
}
