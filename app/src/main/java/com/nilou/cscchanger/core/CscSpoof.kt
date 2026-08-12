package com.nilou.cscchanger.core

/**
 * 核心欺骗逻辑：根据配置计算某个属性 key 应返回的值。
 * Hook 端在 beforeHookedMethod 中调用 [match] 决定是否覆盖返回值。
 */
object CscSpoof {

    /**
     * 匹配系统属性：
     * @return 需要覆盖的新值；返回 null 表示不干预（保持原始值）。
     */
    fun matchProp(key: String, config: CscConfig): String? {
        if (!config.enabled) return null
        val kind = CscConstants.SPOOFED_PROPS[key] ?: return config.customProps[key]
        return when (kind) {
            CscConstants.PropKind.SALES_CODE -> {
                if (config.spoofSalesCode) config.salesCode.takeIf { it.isNotBlank() } else null
            }
            CscConstants.PropKind.COUNTRY_CODE -> {
                if (config.spoofCountryCode) {
                    // 用户显式填了国家码就用它，否则尝试从销售代码推导
                    (config.countryCode.takeIf { it.isNotBlank() }
                        ?: CscConstants.countryCodeOf(config.salesCode))?.takeIf { it.isNotBlank() }
                } else null
            }
        }
    }

    /**
     * 匹配 ril.sales_code：仅当销售代码欺骗开启时使用。
     */
    fun matchRilSalesCode(config: CscConfig): String? {
        if (!config.enabled || !config.spoofRilSalesCode) return null
        return config.salesCode.takeIf { it.isNotBlank() }
    }

    /**
     * 匹配 CscFeature 读取：
     * 先看用户自定义覆盖表，若配置了该 key 则返回覆盖值。
     * @param key CscFeature_* 键名
     */
    fun matchCscFeature(key: String, config: CscConfig): String? {
        if (!config.enabled || !config.spoofSemCscFeature) return null
        return config.customCscFeatures[key]
    }

    /**
     * 匹配机型伪装相关系统属性（Build.MODEL/DEVICE/PRODUCT 等）。
     * @return 需要覆盖的新值；返回 null 表示不干预。
     */
    fun matchDeviceProp(key: String, config: CscConfig): String? {
        if (!config.enabled || !config.spoofDevice) return null
        val profile = DeviceModels.findByName(config.deviceName)
        // 自定义字段优先；为空时 fallback 到 profile
        val model = config.customModel.takeIf { it.isNotBlank() } ?: profile?.model ?: return null
        val device = config.customDevice.takeIf { it.isNotBlank() } ?: profile?.device ?: return null
        val productName = config.customProductName.takeIf { it.isNotBlank() } ?: profile?.productName ?: return null
        val marketName = config.customMarketName.takeIf { it.isNotBlank() } ?: profile?.name ?: return null
        return when (key) {
            "ro.product.model" -> model
            "ro.product.device" -> device
            "ro.build.product" -> device
            "ro.product.name" -> productName
            "ro.product.odm.model" -> model
            "ro.product.vendor.model" -> model
            "ro.product.marketname" -> marketName
            "ro.product.product.model" -> model
            else -> null
        }
    }

    /**
     * 匹配 Build 类静态字段读取（android.os.Build.MODEL 等，部分 App 直接读字段）。
     * 由于 Build 字段是编译期常量，这里仅做标记，实际 hook 在 MainHook 中处理。
     */
    fun deviceProfileFor(config: CscConfig): DeviceProfile? {
        if (!config.enabled || !config.spoofDevice) return null
        return DeviceModels.findByName(config.deviceName)
    }
}
