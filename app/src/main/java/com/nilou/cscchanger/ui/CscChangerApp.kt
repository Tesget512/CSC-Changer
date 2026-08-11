package com.nilou.cscchanger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.nilou.cscchanger.ui.screens.HomeScreen
import com.nilou.cscchanger.ui.screens.SearchScreen
import com.nilou.cscchanger.ui.screens.SettingsScreen

/** 导航目标索引（Int 可被 rememberSaveable 保存进 Bundle，避免自定义类型序列化崩溃） */
private const val SCREEN_HOME = 0
private const val SCREEN_SETTINGS = 1
private const val SCREEN_SEARCH = 2

/** 模块 UI 根组件 */
@Composable
fun CscChangerApp() {
    var screenIndex by rememberSaveable { mutableIntStateOf(SCREEN_HOME) }
    var searchInitialQuery by rememberSaveable { mutableStateOf("") }
    var pendingSelection by remember { mutableStateOf<Pair<String, String>?>(null) }

    when (screenIndex) {
        SCREEN_HOME -> HomeScreen(
            pendingSelection = pendingSelection,
            onConsumeSelection = { pendingSelection = null },
            onOpenSearch = {
                searchInitialQuery = ""
                screenIndex = SCREEN_SEARCH
            },
            onOpenSettings = { screenIndex = SCREEN_SETTINGS },
        )
        SCREEN_SETTINGS -> SettingsScreen(
            onBack = { screenIndex = SCREEN_HOME },
        )
        SCREEN_SEARCH -> SearchScreen(
            initialQuery = searchInitialQuery,
            onBack = { screenIndex = SCREEN_HOME },
            onSelect = { code, region ->
                pendingSelection = code to region
                screenIndex = SCREEN_HOME
            },
        )
    }
}
