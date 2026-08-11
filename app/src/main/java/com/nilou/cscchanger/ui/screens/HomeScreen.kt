package com.nilou.cscchanger.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nilou.cscchanger.MainActivity
import com.nilou.cscchanger.R
import com.nilou.cscchanger.core.CscConfig
import com.nilou.cscchanger.core.CscConstants
import com.nilou.cscchanger.core.CscPrefs
import com.nilou.cscchanger.core.CscSim
import com.nilou.cscchanger.core.DeviceModels
import com.nilou.cscchanger.core.LanguageManager
import com.nilou.cscchanger.core.StoreCleaner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    pendingSelection: Pair<String, String>?,
    onConsumeSelection: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var config by remember { mutableStateOf(CscPrefs.load(context)) }
    // 真实设备信息：每次进入都重新读取，避免 rememberSaveable 冻结旧值。
    // 模块进程不在 LSPosed 作用域内，readSystemProp 读到的是未伪装的真实值。
    val currentSalesCode = remember { readSystemProp(CscConstants.PROP_SALES_CODE) }
    val currentCountryCode = remember { readCountryCodeIso() }
    val currentSimMcc = remember { readSimOperator() }
    val currentSimIso = remember { readSimCountryIso() }

    // onClick 等非 Composable 上下文中要用的字符串（提前在 Composable 中解析）
    val savedToast = stringResource(R.string.saved_toast)
    val clearSuccessToast = stringResource(R.string.clear_store_success)
    val clearFailToast = stringResource(R.string.clear_store_fail)

    // 接收搜索页选中的 CSC：自动推导地区码 + SIM MCC/ISO（始终跟随所选 CSC）
    LaunchedEffect(pendingSelection) {
        if (pendingSelection != null) {
            val (code, _) = pendingSelection ?: return@LaunchedEffect
            val newCountry = CscConstants.countryCodeOf(code) ?: ""
            val newIso = newCountry.lowercase()
            val newMcc = CscSim.mccOfSalesCode(code) ?: ""
            config = config.copy(
                salesCode = code,
                countryCode = newCountry,          // 始终跟随
                simMcc = newMcc,                   // 始终跟随
                simCountryIso = newIso,            // 始终跟随
                simMnc = config.simMnc.ifBlank { "01" },
            )
            onConsumeSelection()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.topbar_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    // 地球图标：切换中英文
                    IconButton(
                        onClick = {
                            LanguageManager.toggle(context)
                            (context as? MainActivity)?.restartForLanguage()
                        },
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = stringResource(R.string.nav_language),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 总开关
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.module_switch), style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (config.enabled) stringResource(R.string.module_enabled) else stringResource(R.string.module_disabled),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = { config = config.copy(enabled = it) },
                    )
                }
            }

            // 当前设备信息（真实值 vs 伪装后的目标值）
            SectionCard(
                title = stringResource(R.string.current_device),
                icon = Icons.Default.PhoneAndroid,
            ) {
                DeviceRow(
                    stringResource(R.string.sales_code_label),
                    real = currentSalesCode,
                    target = config.salesCode.takeIf { it.isNotBlank() && config.spoofSalesCode },
                )
                DeviceRow(
                    stringResource(R.string.region_code_label),
                    real = currentCountryCode,
                    target = config.countryCode.takeIf { it.isNotBlank() && config.spoofCountryCode },
                )
                DeviceRow(
                    "SIM MCC",
                    real = currentSimMcc,
                    target = (if (config.spoofSimInfo) CscSim.resolveMcc(config) else null)
                        .takeIf { it != currentSimMcc },
                )
                DeviceRow(
                    stringResource(R.string.spoof_region),
                    real = currentSimIso,
                    target = (if (config.spoofSimInfo) CscSim.resolveCountryIso(config) else null)
                        .takeIf { it != currentSimIso },
                )
                val profile = if (config.spoofDevice) DeviceModels.findByName(config.deviceName) else null
                DeviceRow(
                    stringResource(R.string.model_no),
                    real = readSystemProp("ro.product.model"),
                    target = profile?.model?.takeIf { it != readSystemProp("ro.product.model") },
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.device_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ===== 目标地区（CSC + 地区代码） =====
            SectionCard(
                title = stringResource(R.string.target_region),
                icon = Icons.Default.Public,
                emphasized = true,
            ) {
                OutlinedButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (config.salesCode.isBlank()) stringResource(R.string.select_sales_code)
                            else "${config.salesCode}  ·  ${regionLabel(config.salesCode)}",
                            modifier = Modifier.weight(1f),
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
                OutlinedTextField(
                    value = config.salesCode,
                    onValueChange = {
                        val newCode = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(3)
                        val newCountry = CscConstants.countryCodeOf(newCode) ?: ""
                        config = config.copy(
                            salesCode = newCode,
                            countryCode = newCountry,
                        )
                    },
                    label = { Text(stringResource(R.string.sales_code_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = config.countryCode,
                    onValueChange = { config = config.copy(countryCode = it.uppercase().take(2)) },
                    label = { Text(stringResource(R.string.region_code_label)) },
                    placeholder = { Text(stringResource(R.string.region_code_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(R.string.quick_regions),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CscConstants.QUICK_REGIONS.forEach { (code, iso, label) ->
                        SuggestionChip(
                            onClick = {
                                config = config.copy(
                                    salesCode = code,
                                    countryCode = iso,
                                    simMcc = CscSim.mccOfSalesCode(code) ?: config.simMcc,
                                    simCountryIso = iso.lowercase(),
                                    simMnc = config.simMnc.ifBlank { "01" },
                                )
                            },
                            label = { Text("$code · $label") },
                        )
                    }
                }
            }

            // ===== SIM 伪装（主页放置，与 CSC 同等重要） =====
            SectionCard(
                title = stringResource(R.string.sim_spoof),
                icon = Icons.Default.SimCard,
                trailing = {
                    Switch(
                        checked = config.spoofSimInfo,
                        onCheckedChange = { config = config.copy(spoofSimInfo = it) },
                    )
                },
            ) {
                Text(
                    stringResource(R.string.sim_spoof_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (config.spoofSimInfo) {
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    CurrentCscRow(stringResource(R.string.real_mcc), currentSimMcc)
                    CurrentCscRow(stringResource(R.string.spoof_mcc), config.simMcc.ifBlank { stringResource(R.string.follow_csc) })
                    CurrentCscRow("MNC", config.simMnc.ifBlank { "01" })
                    CurrentCscRow(stringResource(R.string.spoof_region), config.simCountryIso.ifBlank { CscSim.resolveCountryIso(config) ?: stringResource(R.string.follow_csc) })
                    OutlinedTextField(
                        value = config.simMcc,
                        onValueChange = {
                            config = config.copy(simMcc = it.filter { c -> c.isDigit() }.take(3))
                        },
                        label = { Text(stringResource(R.string.mcc_label)) },
                        placeholder = { Text(stringResource(R.string.mcc_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ===== 机型伪装 =====
            SectionCard(
                title = stringResource(R.string.device_spoof),
                icon = Icons.Default.Smartphone,
                trailing = {
                    Switch(
                        checked = config.spoofDevice,
                        onCheckedChange = { config = config.copy(spoofDevice = it) },
                    )
                },
            ) {
                Text(
                    stringResource(R.string.device_spoof_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (config.spoofDevice) {
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    val currentModel = readSystemProp("ro.product.model")
                    CurrentCscRow(stringResource(R.string.real_model), currentModel)
                    config.deviceName.takeIf { it.isNotBlank() }?.let { name ->
                        DeviceModels.findByName(name)?.let { p ->
                            CurrentCscRow(stringResource(R.string.spoof_model), p.displayName)
                            CurrentCscRow(stringResource(R.string.model_no), p.model)
                            CurrentCscRow(stringResource(R.string.codename), p.device)
                        }
                    }
                    DeviceModels.BY_FAMILY.forEach { (family, models) ->
                        Text(
                            family,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            models.forEach { profile ->
                                FilterChip(
                                    onClick = { config = config.copy(deviceName = profile.name) },
                                    label = { Text(profile.name.removePrefix("Galaxy ")) },
                                    selected = config.deviceName == profile.name,
                                )
                            }
                        }
                    }
                }
            }

            // 保存
            FilledTonalButton(
                onClick = {
                    CscPrefs.save(context, config)
                    scope.launch { snackbarHostState.showSnackbar(savedToast) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.save_config),
                    maxLines = 1,
                    softWrap = false,
                )
            }

            // ===== 清理商店数据 =====
            SectionCard(
                title = stringResource(R.string.clear_store),
                icon = Icons.Default.CleaningServices,
            ) {
                Text(
                    stringResource(R.string.clear_store_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 主按钮：FilledTonalButton（MD3 主操作样式）
                    FilledTonalButton(
                        onClick = {
                            val ok = StoreCleaner.clearStoreDataRoot()
                            if (ok) {
                                context.startActivity(
                                    context.packageManager.getLaunchIntentForPackage(StoreCleaner.GALAXY_STORE)
                                )
                                scope.launch { snackbarHostState.showSnackbar(clearSuccessToast) }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar(clearFailToast) }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.clear_store_now),
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                    // 次按钮：OutlinedButton（MD3 次操作样式）
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(StoreCleaner.buildStoreAppInfoIntent())
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.clear_store_settings),
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }

            HorizontalDivider()

            // 生效说明
            Text(
                stringResource(R.string.effect_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CurrentCscRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value.ifBlank { "未知" },
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
        )
    }
}

/** 真实值 → 伪装目标值 的对比行；target 为空时不显示箭头 */
@Composable
private fun DeviceRow(label: String, real: String, target: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (target != null && target.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    real.ifBlank { "?" },
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text("→", color = MaterialTheme.colorScheme.outline)
                Text(
                    target,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            Text(
                real.ifBlank { "未知" },
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

/** 根据 CSC 代码给出常用地区标签（数据目录中的地区文本） */
private fun regionLabel(code: String): String = when (code.uppercase()) {
    "TGY" -> "香港"
    "BRI" -> "台湾"
    "CHC", "CHM" -> "中国大陆"
    "KOO" -> "韩国"
    "XSP" -> "新加坡"
    "XSE", "XID" -> "印尼"
    "THL" -> "泰国"
    "XME" -> "马来西亚"
    "XTE", "XTC" -> "菲律宾"
    "XXV" -> "越南"
    "INS", "INU" -> "印度"
    "DBT" -> "德国"
    "XEF" -> "法国"
    "ITV" -> "意大利"
    "BTU", "XEU" -> "英国"
    "XAA", "TMB" -> "美国"
    "XSA" -> "澳大利亚 / 沙特"
    "XSG" -> "阿联酋"
    else -> ""
}

/** MD3 风格 section 卡片：标题前带图标、ElevatedCard 容器、可高亮 */
@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    emphasized: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = if (emphasized) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else CardDefaults.elevatedCardColors(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                trailing?.invoke()
            }
            content()
        }
    }
}

/**
 * 读取真实系统属性。
 * MainHook 已对 lpparam.packageName == MODULE_PACKAGE 的进程跳过，
 * 所以此处不会被 hook 拦截。
 */
fun readSystemProp(key: String): String = runCatching {
    val clazz = Class.forName("android.os.SystemProperties")
    val method = clazz.getMethod("get", String::class.java, String::class.java)
    method.invoke(null, key, "") as? String ?: ""
}.getOrDefault("")

/** 常见国家英文名 -> ISO-2 码（三星设备 ro.csc.country_code 可能返回完整英文名） */
private val COUNTRY_NAME_TO_ISO: Map<String, String> = mapOf(
    "China" to "CN", "Hong Kong" to "HK", "Taiwan" to "TW", "Macau" to "MO",
    "Korea" to "KR", "Japan" to "JP",
    "Singapore" to "SG", "Indonesia" to "ID", "Thailand" to "TH", "Malaysia" to "MY",
    "Philippines" to "PH", "Vietnam" to "VN", "United Arab Emirates" to "AE",
    "Israel" to "IL", "India" to "IN", "Pakistan" to "PK",
    "Germany" to "DE", "United Kingdom" to "GB", "France" to "FR", "Italy" to "IT",
    "Poland" to "PL", "Spain" to "ES", "Turkey" to "TR", "Switzerland" to "CH",
    "Sweden" to "SE", "Norway" to "NO", "Netherlands" to "NL", "Belgium" to "BE",
    "Portugal" to "PT", "Ireland" to "IE", "Austria" to "AT", "Czech" to "CZ",
    "Greece" to "GR", "Hungary" to "HU", "Romania" to "RO",
    "United States" to "US", "USA" to "US", "Canada" to "CA", "Mexico" to "MX",
    "Brazil" to "BR", "Australia" to "AU", "New Zealand" to "NZ",
    "Saudi Arabia" to "SA", "Egypt" to "EG", "South Africa" to "ZA", "Russia" to "RU",
)

/** 读取真实地区代码并规范化为 ISO-2 码（如 China -> CN） */
fun readCountryCodeIso(): String {
    val raw = readSystemProp(CscConstants.PROP_COUNTRY_CODE).ifBlank { return "?" }
    // 已经是 ISO-2（大写字母）则直接返回
    if (raw.length == 2 && raw.all { it.isLetter() }) return raw.uppercase()
    // 完整英文名 -> ISO
    return COUNTRY_NAME_TO_ISO[raw] ?: raw
}

/** 反射读取真实 SIM MCC（gsm.sim.operator.numeric 前3位） */
fun readSimOperator(): String = runCatching {
    val op = readSystemProp("gsm.sim.operator.numeric")
    op.takeIf { it.length >= 3 }?.substring(0, 3) ?: "?"
}.getOrDefault("?")

/** 反射读取真实 SIM 国家 ISO（gsm.sim.operator.iso-country） */
fun readSimCountryIso(): String = runCatching {
    readSystemProp("gsm.sim.operator.iso-country").ifBlank { "?" }
}.getOrDefault("?")
