// AGP 9.x 已内建 Kotlin 支持，无需 org.jetbrains.kotlin.android 插件
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.nilou.cscchanger"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.nilou.cscchanger"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // 正式签名：从 local.properties 读取（不入库）。
        // 缺失时回退到 debug keystore，保证仓库克隆后仍可构建。
        create("release") {
            val props = Properties().apply {
                val f = file("../local.properties")
                if (f.exists()) f.inputStream().use { load(it) }
            }
            storeFile = props.getProperty("signing.storeFile")?.let { file(it) }
                ?: file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = props.getProperty("signing.storePassword") ?: "android"
            keyAlias = props.getProperty("signing.keyAlias") ?: "androiddebugkey"
            keyPassword = props.getProperty("signing.keyPassword") ?: "android"
        }
    }

    buildTypes {
        release {
            // 不做混淆（模块代码保持可读，便于排查 hook 问题）
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    // AGP 9 内建 Kotlin：jvmTarget 自动跟随 compileOptions
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources {
        // LSPosed 模块：自定义资源 ID 前缀，避免与宿主冲突
        additionalParameters += listOf(
            "--allow-reserved-package-id",
            "--package-id",
            "0x55"
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // libxposed 现代 API（102）：把 META-INF/xposed/* 打进 APK
            //（module.prop / java_init.list 是模块入口声明）
            merges += "META-INF/xposed/*"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // libxposed 现代 API（102）：仅编译期依赖。
    // targetApiVersion=102 的模块禁止调用 legacy de.robv.android.xposed API，
    // 因此这里只保留 libxposed api，不再依赖旧 xposed-api。
    compileOnly(libs.libxposed.api)
    // libxposed service：UI 侧进程绑定 XposedService，用 getRemotePreferences 写配置，
    // 与 hook 侧 getRemotePreferences 读配置走同一份框架存储（否则 SELinux 读不到）。
    implementation(libs.libxposed.service)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
}
