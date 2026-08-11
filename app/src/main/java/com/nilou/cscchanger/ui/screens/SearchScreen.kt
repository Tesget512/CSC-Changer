package com.nilou.cscchanger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nilou.cscchanger.R
import com.nilou.cscchanger.core.CscConstants
import com.nilou.cscchanger.data.CscCatalog

/** 常用 CSC 快捷入口 */
private val QUICK_CODES = listOf("TGY", "BRI", "CHC", "KOO", "XSP", "DBT", "XAA")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String,
    onBack: () -> Unit,
    onSelect: (code: String, region: String) -> Unit,
) {
    val context = LocalContext.current
    val catalog = remember { CscCatalog.load(context) }

    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var searchInput by rememberSaveable { mutableStateOf(initialQuery) }

    // 搜索输入防抖：立即执行（本地列表很小，无需防抖）
    val results = remember(query) { CscCatalog.search(catalog, query) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_csc_title)) },
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
                .padding(padding),
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = {
                    searchInput = it
                    query = it
                },
                label = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchInput.isNotEmpty()) {
                        IconButton(onClick = { searchInput = ""; query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // 常用快捷
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QUICK_CODES.forEach { code ->
                    SuggestionChip(
                        onClick = {
                            val entry = catalog.find { it.code == code }
                            onSelect(code, entry?.region ?: "")
                        },
                        label = { Text(code) },
                    )
                }
            }

            HorizontalDivider(Modifier.padding(top = 8.dp))

            Text(
                stringResource(R.string.search_result_count, results.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(results, key = { it.code }) { entry ->
                    ListItem(
                        headlineContent = {
                            Text(
                                entry.code,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        supportingContent = { Text(entry.region) },
                        trailingContent = {
                            entry.countryCode?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(),
                        modifier = Modifier.clickable {
                            onSelect(entry.code, entry.region)
                        },
                    )
                }
            }
        }
    }
}
