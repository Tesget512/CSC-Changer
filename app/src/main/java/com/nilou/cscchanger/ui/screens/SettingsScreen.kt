package com.nilou.cscchanger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.nilou.cscchanger.R
import com.nilou.cscchanger.core.CscConfig
import com.nilou.cscchanger.core.CscPrefs
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var config by remember { mutableStateOf(CscPrefs.load(context)) }
    var customPropsText by rememberSaveable { mutableStateOf(config.customProps.toJson()) }
    var customFeaturesText by rememberSaveable { mutableStateOf(config.customCscFeatures.toJson()) }
    var error by remember { mutableStateOf<String?>(null) }

    val jsonErrorMsg = stringResource(R.string.json_error)
    val settingsSavedMsg = stringResource(R.string.settings_saved)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            // 欺骗项开关
            Card {
                Column(Modifier.padding(vertical = 4.dp)) {
                    SettingSwitch(
                        title = "欺骗 ro.csc.sales_code",
                        subtitle = "主销售代码（Galaxy Store 识别区域的主要依据）",
                        checked = config.spoofSalesCode,
                        onChecked = { config = config.copy(spoofSalesCode = it) },
                    )
                    SettingSwitch(
                        title = "欺骗 ro.csc.country_code",
                        subtitle = "地区代码（如 CN / HK / TW）",
                        checked = config.spoofCountryCode,
                        onChecked = { config = config.copy(spoofCountryCode = it) },
                    )
                    SettingSwitch(
                        title = "欺骗 ril.sales_code",
                        subtitle = "SIM / 运营商销售代码",
                        checked = config.spoofRilSalesCode,
                        onChecked = { config = config.copy(spoofRilSalesCode = it) },
                    )
                    SettingSwitch(
                        title = "欺骗 OMC 相关属性",
                        subtitle = "ro.csc.omcnw_code / official_csc / csc_code",
                        checked = config.spoofOmcCodes,
                        onChecked = { config = config.copy(spoofOmcCodes = it) },
                    )
                    SettingSwitch(
                        title = "hook SemCscFeature",
                        subtitle = "拦截 CscFeature_* 读取（需自定义覆盖表）",
                        checked = config.spoofSemCscFeature,
                        onChecked = { config = config.copy(spoofSemCscFeature = it) },
                    )
                }
            }

            // SIM 欺骗说明（配置在主页）
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    stringResource(R.string.sim_note),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                )
            }

            // 自定义属性覆盖
            Card {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(R.string.custom_props), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.custom_props_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = customPropsText,
                        onValueChange = { customPropsText = it },
                        placeholder = { Text("{\"ro.csc.custom\":\"value\"}") },
                        minLines = 3,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // 自定义 CscFeature 覆盖
            Card {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(R.string.custom_features), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.custom_features_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = customFeaturesText,
                        onValueChange = { customFeaturesText = it },
                        placeholder = { Text("{\"CscFeature_Common_ConfigSamsungLegal\":\"...\"}") },
                        minLines = 3,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // 保存
            TextButton(
                onClick = {
                    error = null
                    val props = runCatching { customPropsText.trim().ifEmpty { "{}" }.toMap() }
                        .getOrElse { error = jsonErrorMsg; return@TextButton }
                    val features = runCatching { customFeaturesText.trim().ifEmpty { "{}" }.toMap() }
                        .getOrElse { error = jsonErrorMsg; return@TextButton }

                    val final = config.copy(customProps = props, customCscFeatures = features)
                    CscPrefs.save(context, final)
                    scope.launch { snackbarHostState.showSnackbar(settingsSavedMsg) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
                Text(stringResource(R.string.save_settings))
            }

            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // 作用域说明
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    stringResource(R.string.scope_note),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

/** Map -> 格式化 JSON 文本 */
private fun Map<String, String>.toJson(): String =
    JSONObject(this).toString()

/** JSON 文本 -> Map */
private fun String.toMap(): Map<String, String> {
    val obj = JSONObject(this)
    val keys = obj.keys()
    return buildMap {
        while (keys.hasNext()) {
            val k = keys.next()
            put(k, obj.optString(k))
        }
    }
}
