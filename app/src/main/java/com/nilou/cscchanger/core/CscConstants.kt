package com.nilou.cscchanger.core

/**
 * 三星 CSC 相关常量：属性名、类名、常见国家码映射。
 *
 * 依据：
 * - KnoxPatch PropSpoofHooks.kt 证实 `android.os.SystemProperties` /
 *   `android.os.SemSystemProperties` 的 get 是三星 App 读取区域属性的入口
 * - OneUIX 证实 `com.samsung.android.feature.SemCscFeature` 是 CscFeature 读取入口
 */
object CscConstants {

    const val PREFS_NAME = "csc_config"

    /** 模块包名，用于定位配置文件的存储目录 */
    const val MODULE_PACKAGE = "com.nilou.cscchanger"

    // ---- 需要欺骗的系统属性 ----
    /** 主销售代码，如 CHC(中国) / TGY(香港) / BRI(台湾) / KOO(韩国) */
    const val PROP_SALES_CODE = "ro.csc.sales_code"
    /** 地区代码，如 CN / HK / TW / KR */
    const val PROP_COUNTRY_CODE = "ro.csc.country_code"
    /** SIM / 运营商销售代码 */
    const val PROP_RIL_SALES_CODE = "ril.sales_code"
    /** OMC 网络代码（多 CSC 机型） */
    const val PROP_OMCNW_CODE = "ro.csc.omcnw_code"
    /** 官方 CSC（出厂值） */
    const val PROP_OFFICIAL_CSC = "ro.csc.official_csc"
    /** CSC 代码 */
    const val PROP_CSC_CODE = "ro.csc.csc_code"

    /** 属性 hook 主列表：key -> 取 salesCode 还是 countryCode 替换 */
    val SPOOFED_PROPS: Map<String, PropKind> = mapOf(
        PROP_SALES_CODE to PropKind.SALES_CODE,
        PROP_RIL_SALES_CODE to PropKind.SALES_CODE,
        PROP_OMCNW_CODE to PropKind.SALES_CODE,
        PROP_OFFICIAL_CSC to PropKind.SALES_CODE,
        PROP_CSC_CODE to PropKind.SALES_CODE,
        PROP_COUNTRY_CODE to PropKind.COUNTRY_CODE,
    )

    enum class PropKind { SALES_CODE, COUNTRY_CODE }

    // ---- 需要 hook 的类 ----
    const val CLASS_SYSTEM_PROPERTIES = "android.os.SystemProperties"
    const val CLASS_SEM_SYSTEM_PROPERTIES = "android.os.SemSystemProperties"
    const val CLASS_SEM_CSC_FEATURE = "com.samsung.android.feature.SemCscFeature"

    /**
     * 常用 CSC -> 地区码（ISO 3166-1 二位）映射。
     * 仅用于 UI 中根据所选 CSC 自动填充地区码，hook 时不依赖此表。
     */
    val COUNTRY_CODES: Map<String, String> = mapOf(
        // 大中华区
        "CHC" to "CN", "CHM" to "CN", "CHN" to "CN", "TGY" to "HK", "BRI" to "TW",
        "CWT" to "TW", "TWM" to "TW", "FET" to "TW", "TGY" to "HK",
        // 韩国 / 日本
        "KOO" to "KR", "SKT" to "KR", "KTC" to "KR", "LUC" to "KR", "KTT" to "KR",
        "LGT" to "KR", "JDI" to "JP", "DCM" to "JP", "KDI" to "JP", "XJP" to "JP",
        "SJP" to "JP", "DCM" to "JP",
        // 东南亚
        "XSP" to "SG", "MM1" to "SG", "SIN" to "SG", "XID" to "ID", "XSE" to "ID",
        "THL" to "TH", "THO" to "TH", "XME" to "MY", "XTE" to "PH", "XTC" to "PH",
        "XXV" to "VN", "XSG" to "AE", "XSI" to "IL", "XPH" to "PH", "XID" to "ID",
        "XEV" to "VN", "XLA" to "LA", "XKH" to "KH", "XMM" to "MM",
        // 南亚
        "INS" to "IN", "INU" to "IN", "OMD" to "IN", "OMF" to "IN", "OMS" to "IN",
        "PAK" to "PK", "NPB" to "NP", "SLK" to "LK", "BGD" to "BD",
        // 欧洲
        "DBT" to "DE", "XEG" to "DE", "XEH" to "HU", "BTU" to "GB", "XEU" to "GB",
        "ITV" to "IT", "XEF" to "FR", "XEO" to "PL", "XEZ" to "CZ", "AUT" to "CH",
        "ATO" to "AT", "EUX" to "AT", "EUY" to "AT", "LUX" to "LU", "MOB" to "AT",
        "TRG" to "TR", "TUR" to "TR", "PHE" to "ES", "AMO" to "ES", "XEC" to "ES",
        "SEB" to "SE", "NEE" to "SE", "TEN" to "NO", "NBS" to "NO", "XSK" to "SK",
        "XSG" to "AE", "XSA" to "SA", "DBT" to "DE", "VIA" to "IT", "XTE" to "PH",
        "XEO" to "PL", "PRT" to "PT", "PHN" to "NL", "LUX" to "LU", "XEH" to "HU",
        "ROU" to "RO", "BHT" to "BA", "SEE" to "RS", "CRO" to "HR", "SIO" to "SI",
        "OMN" to "OM", "KSA" to "SA", "AFR" to "AE",
        // 美洲
        "TPA" to "US", "TMB" to "US", "VZW" to "US", "ATT" to "US", "XAA" to "US",
        "CSC" to "US", "MXO" to "MX", "TTT" to "TT", "ZTO" to "BR", "ZTM" to "BR",
        "ZTA" to "BR", "ZVV" to "BR", "ZTR" to "BR", "ZTO" to "BR", "ZTM" to "BR",
        "ZVV" to "BR", "ZTA" to "BR", "UPO" to "UY", "CHL" to "CL", "COL" to "CO",
        "PER" to "PE", "ARG" to "AR", "MEX" to "MX",
        // 大洋洲
        "AUS" to "AU", "XSA" to "AU", "VAU" to "AU", "TEL" to "AU", "NZC" to "NZ",
        "NZC" to "NZ", "VNZ" to "NZ",
        // 中东 / 非洲
        "EGY" to "EG", "XFA" to "SA", "MRK" to "MA", "DZR" to "DZ", "TUN" to "TN",
        "XFV" to "ZA", "XFE" to "ZA", "XFM" to "ZA", "MID" to "IQ", "ILO" to "IL",
        "XSA" to "SA", "XSG" to "AE", "XSI" to "IL", "KSA" to "SA", "AFR" to "EG",
        "XFA" to "SA", "XFM" to "ZA", "XFV" to "ZA", "XFE" to "ZA",
        // 俄罗斯 / 独联体
        "SER" to "RU", "SKZ" to "KZ", "CAC" to "KZ", "BYK" to "BY", "SEK" to "UA",
        "CAU" to "GE", "AZC" to "AZ",
    )

    /** 常用地区快捷入口（搜索页/主页展示） */
    val QUICK_REGIONS: List<Triple<String, String, String>> = listOf(
        Triple("TGY", "HK", "香港"),
        Triple("BRI", "TW", "台湾"),
        Triple("CHC", "CN", "中国大陆"),
        Triple("KOO", "KR", "韩国"),
        Triple("XSP", "SG", "新加坡"),
        Triple("XID", "ID", "印尼"),
        Triple("THL", "TH", "泰国"),
        Triple("XME", "MY", "马来西亚"),
        Triple("XTE", "PH", "菲律宾"),
        Triple("XXV", "VN", "越南"),
        Triple("DBT", "DE", "德国"),
        Triple("XEF", "FR", "法国"),
        Triple("ITV", "IT", "意大利"),
        Triple("BTU", "GB", "英国"),
        Triple("XEU", "GB", "英国(欧盟)"),
        Triple("XAA", "US", "美国"),
        Triple("XSA", "AU", "澳大利亚"),
        Triple("XSG", "AE", "阿联酋"),
        Triple("INS", "IN", "印度"),
        Triple("XFV", "ZA", "南非"),
    )

    /** 根据销售代码推导国家码，失败返回 null */
    fun countryCodeOf(salesCode: String): String? = COUNTRY_CODES[salesCode.uppercase()]
}
