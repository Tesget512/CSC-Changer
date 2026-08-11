package com.nilou.cscchanger.core

import org.json.JSONObject

/**
 * 模块配置模型。
 * UI 侧写入 SharedPreferences，Hook 侧解析配置文件还原。
 */
data class CscConfig(
    /** 总开关 */
    val enabled: Boolean = true,
    /** 目标销售代码（CSC），如 TGY / BRI / CHC */
    val salesCode: String = "",
    /** 目标地区代码，如 HK / TW / CN */
    val countryCode: String = "",
    /** 是否欺骗 ro.csc.sales_code */
    val spoofSalesCode: Boolean = true,
    /** 是否欺骗 ro.csc.country_code */
    val spoofCountryCode: Boolean = true,
    /** 是否欺骗 ril.sales_code */
    val spoofRilSalesCode: Boolean = true,
    /** 是否欺骗 ro.csc.omcnw_code / official_csc / csc_code */
    val spoofOmcCodes: Boolean = true,
    /** 是否 hook SemCscFeature（CscFeature_* 读取） */
    val spoofSemCscFeature: Boolean = true,
    /** 是否欺骗 SIM 信息（TelephonyManager：MCC/MNC/国家ISO）。影响 Galaxy Store 等按 SIM 判区的应用 */
    val spoofSimInfo: Boolean = false,
    /** 目标 SIM MCC（3位数字，如 454=香港/466=台湾/999=占位），用于替换 gsm.sim.operator 等 */
    val simMcc: String = "",
    /** 目标 SIM MNC */
    val simMnc: String = "",
    /** 目标 SIM 国家 ISO（如 hk / tw / kr），用于 getSimCountryIso */
    val simCountryIso: String = "",
    /** 是否启用机型伪装（Build.MODEL/DEVICE/PRODUCT） */
    val spoofDevice: Boolean = false,
    /** 目标机型展示名（如 "Galaxy Z Fold8"），与 DeviceModels 对应 */
    val deviceName: String = "",
    /** 自定义属性覆盖：key -> value */
    val customProps: Map<String, String> = emptyMap(),
    /** 自定义 CscFeature 覆盖：key -> value */
    val customCscFeatures: Map<String, String> = emptyMap(),
) {

    companion object {
        private const val KEY_ENABLED = "enabled"
        private const val KEY_SALES_CODE = "sales_code"
        private const val KEY_COUNTRY_CODE = "country_code"
        private const val KEY_SPOOF_SALES = "spoof_sales_code"
        private const val KEY_SPOOF_COUNTRY = "spoof_country_code"
        private const val KEY_SPOOF_RIL = "spoof_ril_sales_code"
        private const val KEY_SPOOF_OMC = "spoof_omc_codes"
        private const val KEY_SPOOF_CSC_FEATURE = "spoof_sem_csc_feature"
        private const val KEY_SPOOF_SIM = "spoof_sim_info"
        private const val KEY_SIM_MCC = "sim_mcc"
        private const val KEY_SIM_MNC = "sim_mnc"
        private const val KEY_SIM_ISO = "sim_country_iso"
        private const val KEY_SPOOF_DEVICE = "spoof_device"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_CUSTOM_PROPS = "custom_props"
        private const val KEY_CUSTOM_FEATURES = "custom_csc_features"

        fun jsonToMap(json: String?): Map<String, String> {
            if (json.isNullOrBlank()) return emptyMap()
            return runCatching {
                val obj = JSONObject(json)
                val keys = obj.keys()
                buildMap {
                    while (keys.hasNext()) {
                        val k = keys.next()
                        put(k, obj.optString(k))
                    }
                }
            }.getOrDefault(emptyMap())
        }

        private fun mapToJson(map: Map<String, String>): String =
            JSONObject(map).toString()

        /** 从 SharedPreferences 读取（UI 侧） */
        fun fromPrefs(prefs: android.content.SharedPreferences): CscConfig = CscConfig(
            enabled = prefs.getBoolean(KEY_ENABLED, true),
            salesCode = prefs.getString(KEY_SALES_CODE, "") ?: "",
            countryCode = prefs.getString(KEY_COUNTRY_CODE, "") ?: "",
            spoofSalesCode = prefs.getBoolean(KEY_SPOOF_SALES, true),
            spoofCountryCode = prefs.getBoolean(KEY_SPOOF_COUNTRY, true),
            spoofRilSalesCode = prefs.getBoolean(KEY_SPOOF_RIL, true),
            spoofOmcCodes = prefs.getBoolean(KEY_SPOOF_OMC, true),
            spoofSemCscFeature = prefs.getBoolean(KEY_SPOOF_CSC_FEATURE, true),
            spoofSimInfo = prefs.getBoolean(KEY_SPOOF_SIM, false),
            simMcc = prefs.getString(KEY_SIM_MCC, "") ?: "",
            simMnc = prefs.getString(KEY_SIM_MNC, "") ?: "",
            simCountryIso = prefs.getString(KEY_SIM_ISO, "") ?: "",
            spoofDevice = prefs.getBoolean(KEY_SPOOF_DEVICE, false),
            deviceName = prefs.getString(KEY_DEVICE_NAME, "") ?: "",
            customProps = jsonToMap(prefs.getString(KEY_CUSTOM_PROPS, null)),
            customCscFeatures = jsonToMap(prefs.getString(KEY_CUSTOM_FEATURES, null)),
        )

        /** 写入 SharedPreferences（UI 侧） */
        fun toPrefs(prefs: android.content.SharedPreferences.Editor, config: CscConfig) {
            prefs.putBoolean(KEY_ENABLED, config.enabled)
            prefs.putString(KEY_SALES_CODE, config.salesCode.uppercase())
            prefs.putString(KEY_COUNTRY_CODE, config.countryCode.uppercase())
            prefs.putBoolean(KEY_SPOOF_SALES, config.spoofSalesCode)
            prefs.putBoolean(KEY_SPOOF_COUNTRY, config.spoofCountryCode)
            prefs.putBoolean(KEY_SPOOF_RIL, config.spoofRilSalesCode)
            prefs.putBoolean(KEY_SPOOF_OMC, config.spoofOmcCodes)
            prefs.putBoolean(KEY_SPOOF_CSC_FEATURE, config.spoofSemCscFeature)
            prefs.putBoolean(KEY_SPOOF_SIM, config.spoofSimInfo)
            prefs.putString(KEY_SIM_MCC, config.simMcc.uppercase())
            prefs.putString(KEY_SIM_MNC, config.simMnc.uppercase())
            prefs.putString(KEY_SIM_ISO, config.simCountryIso.lowercase())
            prefs.putBoolean(KEY_SPOOF_DEVICE, config.spoofDevice)
            prefs.putString(KEY_DEVICE_NAME, config.deviceName)
            prefs.putString(KEY_CUSTOM_PROPS, mapToJson(config.customProps))
            prefs.putString(KEY_CUSTOM_FEATURES, mapToJson(config.customCscFeatures))
        }
    }
}
