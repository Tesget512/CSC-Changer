package com.nilou.cscchanger

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nilou.cscchanger.core.LanguageManager
import com.nilou.cscchanger.ui.CscChangerApp
import com.nilou.cscchanger.ui.theme.CscChangerTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.apply(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CscChangerTheme {
                CscChangerApp()
            }
        }
    }

    /** 语言切换后重建 Activity 使新语言生效 */
    fun restartForLanguage() {
        recreate()
    }
}
