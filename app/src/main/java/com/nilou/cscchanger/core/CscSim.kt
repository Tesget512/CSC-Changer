package com.nilou.cscchanger.core

/**
 * SIM MCC 相关辅助：MCC <-> 国家 ISO 映射。
 * Galaxy Store 等按 SIM 主卡 MCC 判定区域的应用，
 * 需要 hook TelephonyManager 时用到这些映射。
 */
object CscSim {

    /** MCC(3位) -> 国家 ISO(2位小写) */
    val MCC_TO_ISO: Map<String, String> = mapOf(
        // 大中华区
        "460" to "cn", "455" to "mo", "466" to "tw", "454" to "hk",
        // 韩国 / 日本
        "450" to "kr", "440" to "jp", "441" to "jp", "442" to "jp", "443" to "jp",
        // 东南亚
        "525" to "sg", "510" to "id", "520" to "th", "502" to "my",
        "515" to "ph", "452" to "vn", "424" to "ae", "425" to "il", "528" to "bn",
        "457" to "la", "456" to "kh", "514" to "mn",
        // 南亚
        "405" to "in", "404" to "in", "410" to "pk", "470" to "bd", "413" to "lk",
        "414" to "mm", "417" to "lk", "418" to "iq",
        // 欧洲
        "262" to "de", "216" to "hu", "234" to "gb", "235" to "gb",
        "222" to "it", "208" to "fr", "260" to "pl", "230" to "cz",
        "228" to "ch", "232" to "at", "286" to "tr", "214" to "es",
        "240" to "se", "242" to "no", "231" to "sk", "272" to "ie",
        "206" to "be", "204" to "nl", "268" to "pt", "213" to "ad",
        "247" to "lv", "246" to "lt", "248" to "ee", "226" to "ro",
        "220" to "rs", "218" to "ba", "219" to "hr", "293" to "si",
        "202" to "gr", "226" to "ro",
        // 中东 / 非洲
        "602" to "eg", "420" to "sa", "603" to "dz", "605" to "tn",
        "655" to "za", "634" to "sd", "604" to "ma", "415" to "lb",
        "415" to "sy", "417" to "lk",
        // 美洲
        "310" to "us", "311" to "us", "312" to "us", "313" to "us",
        "316" to "us", "334" to "mx", "302" to "ca", "724" to "br",
        "730" to "cl", "734" to "ve", "736" to "bo", "732" to "co",
        "716" to "pe", "334" to "mx",
        // 大洋洲
        "505" to "au", "530" to "nz",
        // 俄罗斯 / 独联体
        "250" to "ru", "257" to "by", "255" to "ua", "259" to "md",
        "289" to "ge", "247" to "lv",
    )

    /** ISO -> 常用 MCC（用于 UI 展示默认值） */
    val ISO_TO_MCC: Map<String, String> = MCC_TO_ISO.entries
        .groupBy({ it.value }, { it.key })
        .mapValues { it.value.first() }

    /** 国家码(2位大写) -> ISO(2位小写) 别名映射，用于 CSC 国家码推导 */
    private val COUNTRY_TO_ISO: Map<String, String> = mapOf(
        "CN" to "cn", "HK" to "hk", "TW" to "tw", "MO" to "mo",
        "KR" to "kr", "JP" to "jp",
        "SG" to "sg", "ID" to "id", "TH" to "th", "MY" to "my",
        "PH" to "ph", "VN" to "vn", "AE" to "ae", "IL" to "il",
        "IN" to "in", "PK" to "pk", "BD" to "bd",
        "DE" to "de", "GB" to "gb", "FR" to "fr", "IT" to "it",
        "PL" to "pl", "ES" to "es", "TR" to "tr", "CH" to "ch",
        "SE" to "se", "NO" to "no", "NL" to "nl", "BE" to "be",
        "PT" to "pt", "IE" to "ie", "AT" to "at", "CZ" to "cz",
        "GR" to "gr", "HU" to "hu", "RO" to "ro",
        "US" to "us", "CA" to "ca", "MX" to "mx", "BR" to "br",
        "AU" to "au", "NZ" to "nz",
        "SA" to "sa", "EG" to "eg", "ZA" to "za", "RU" to "ru",
    )

    /** 由销售代码（CSC）推导目标 MCC，失败返回 null */
    fun mccOfSalesCode(salesCode: String): String? {
        val country = CscConstants.countryCodeOf(salesCode) ?: return null
        val iso = COUNTRY_TO_ISO[country] ?: return null
        return ISO_TO_MCC[iso]
    }

    /**
     * 计算目标 SIM 国家 ISO：
     * 1. 用户显式填了 simCountryIso 就用它
     * 2. 否则用 simMcc 查表
     * 3. 否则从 salesCode 推导的国家码（如 TGY->HK->hk）
     */
    fun resolveCountryIso(config: CscConfig): String? {
        config.simCountryIso.takeIf { it.isNotBlank() }?.let { return it.lowercase() }
        config.simMcc.takeIf { it.length >= 3 }?.let { mcc ->
            MCC_TO_ISO[mcc.take(3)]?.let { return it }
        }
        config.salesCode.takeIf { it.isNotBlank() }?.let { code ->
            CscConstants.countryCodeOf(code)?.let { return it.lowercase() }
        }
        return null
    }

    /** 计算目标 SIM MNC（默认取用户值，否则 "01"） */
    fun resolveMnc(config: CscConfig): String =
        config.simMnc.takeIf { it.isNotBlank() } ?: "01"

    /** 计算目标 SIM MCC（默认取用户值） */
    fun resolveMcc(config: CscConfig): String =
        config.simMcc.takeIf { it.isNotBlank() } ?: "460"
}
