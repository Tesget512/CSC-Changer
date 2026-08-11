package com.nilou.cscchanger

import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Log
import com.nilou.cscchanger.core.CscConfig
import com.nilou.cscchanger.core.CscConstants
import com.nilou.cscchanger.core.CscSim
import com.nilou.cscchanger.core.CscSpoof
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * libxposed 现代 API（102）模块入口。
 *
 * 入口声明：`resources/META-INF/xposed/java_init.list`；
 * 作用域：`resources/META-INF/xposed/scope.list` + LSPosed 管理器配置。
 *
 * 与旧 API 的关键差异（targetApiVersion=102 强制）：
 * 1. 禁止调用 legacy `de.robv.android.xposed` 系列 API
 * 2. 跨进程读配置用 [getRemotePreferences]（LSPosed 服务转发，无 SELinux 问题），
 *    不再用 XSharedPreferences / 手动解析 XML
 * 3. hook 用 `hook(method).intercept { chain -> ... }`，返回非 `chain.proceed()`
 *    的值即覆盖原方法
 *
 * 作用域内每个进程（Galaxy Store / 主题商店 / 三星账号等）加载本类，
 * 对以下读取路径进行欺骗：
 * 1. `android.os.SystemProperties.get` —— CSC 区域属性
 * 2. `android.os.SemSystemProperties.get`（三星扩展，不存在则跳过）
 * 3. `com.samsung.android.feature.SemCscFeature` —— CscFeature_* 配置
 * 4. `android.telephony.TelephonyManager` —— SIM MCC/MNC/国家 ISO
 */
class MainHook : XposedModule() {

    override fun onPackageReady(param: PackageReadyParam) {
        // 排除模块自身进程：UI 进程读取属性要展示"原信息"，不能被自己 hook 拦截
        if (param.packageName == CscConstants.MODULE_PACKAGE) return

        val config = ConfigHolder.current(prefs)
        if (!config.enabled) return

        hookSystemProperties(param.classLoader)
        hookSemSystemProperties(param.classLoader)
        hookSemCscFeature(param.classLoader)
        hookTelephony(param.classLoader)

        log(
            Log.INFO, TAG,
            "CscChanger: hooked in ${param.packageName} " +
                "salesCode=${config.salesCode} countryCode=${config.countryCode}"
        )
    }

    // ---------- 配置 ----------

    /**
     * 通过 LSPosed 服务跨进程读取模块配置（返回只读 SharedPreferences）。
     * 由框架服务以 root 转发，不受 SELinux 的 app_data_file 隔离限制。
     */
    private val prefs: SharedPreferences by lazy { getRemotePreferences(CscConstants.PREFS_NAME) }

    // ---- TelephonyManager（SIM 欺骗，影响按 SIM 判区的应用） ----

    private fun hookTelephony(cl: ClassLoader) {
        if (!ConfigHolder.current(prefs).spoofSimInfo) return
        runCatching {
            val tm = Class.forName("android.telephony.TelephonyManager", false, cl)
            // getSimOperator() -> MCC+MNC（如 46001）
            safeHook(tm, "getSimOperator") { chain -> simOperatorIntercept(chain) }
            // getSimOperator(int subId)
            safeHook(tm, "getSimOperator", Int::class.javaPrimitiveType!!) { chain -> simOperatorIntercept(chain) }
            // getSimOperatorNumeric()
            safeHook(tm, "getSimOperatorNumeric") { chain -> simOperatorIntercept(chain) }
            // getNetworkOperator() / getNetworkOperatorNumeric()
            safeHook(tm, "getNetworkOperator") { chain -> simOperatorIntercept(chain) }
            safeHook(tm, "getNetworkOperatorNumeric") { chain -> simOperatorIntercept(chain) }

            // getSimCountryIso() -> cn/hk/tw
            safeHook(tm, "getSimCountryIso") { chain -> simIsoIntercept(chain) }
            safeHook(tm, "getSimCountryIso", Int::class.javaPrimitiveType!!) { chain -> simIsoIntercept(chain) }
            // getNetworkCountryIso()
            safeHook(tm, "getNetworkCountryIso") { chain -> simIsoIntercept(chain) }

            log(
                Log.INFO, TAG,
                "CscChanger: TelephonyManager SIM spoof hooked " +
                    "mcc=${CscSim.resolveMcc(ConfigHolder.current(prefs))} " +
                    "iso=${CscSim.resolveCountryIso(ConfigHolder.current(prefs))}"
            )
        }.onFailure {
            log(Log.WARN, TAG, "CscChanger: TelephonyManager hook failed: $it")
        }
    }

    /** getSimOperator：返回 MCC+MNC（如 45401） */
    private fun simOperatorIntercept(chain: XposedInterface.Chain): Any {
        val cfg = ConfigHolder.current(prefs)
        if (!cfg.spoofSimInfo) return chain.proceed()
        return CscSim.resolveMcc(cfg) + CscSim.resolveMnc(cfg)
    }

    /** getSimCountryIso / getNetworkCountryIso：返回国家 ISO（如 hk） */
    private fun simIsoIntercept(chain: XposedInterface.Chain): Any {
        val cfg = ConfigHolder.current(prefs)
        if (!cfg.spoofSimInfo) return chain.proceed()
        return CscSim.resolveCountryIso(cfg) ?: chain.proceed()
    }

    // ---- SystemProperties ----

    private fun hookSystemProperties(cl: ClassLoader) {
        runCatching {
            val clazz = Class.forName(CscConstants.CLASS_SYSTEM_PROPERTIES, false, cl)
            safeHook(clazz, "get", String::class.java) { chain -> propIntercept(chain) }
            safeHook(clazz, "get", String::class.java, String::class.java) { chain -> propIntercept(chain) }
        }.onFailure {
            log(Log.WARN, TAG, "CscChanger: SystemProperties hook failed: $it")
        }
    }

    private fun propIntercept(chain: XposedInterface.Chain): Any {
        val key = chain.getArg(0) as? String ?: return chain.proceed()
        val cfg = ConfigHolder.current(prefs)
        // 1. CSC 区域属性
        CscSpoof.matchProp(key, cfg)?.let { return it }
        // 2. 机型伪装属性
        CscSpoof.matchDeviceProp(key, cfg)?.let { return it }
        return chain.proceed()
    }

    // ---- SemSystemProperties（三星私有扩展） ----

    private fun hookSemSystemProperties(cl: ClassLoader) {
        runCatching {
            val clazz = Class.forName(CscConstants.CLASS_SEM_SYSTEM_PROPERTIES, false, cl)
            safeHook(clazz, "get", String::class.java) { chain -> propIntercept(chain) }
            safeHook(clazz, "get", String::class.java, String::class.java) { chain -> propIntercept(chain) }
        }.onFailure {
            // 非三星 / 旧版本系统可能没有该类，忽略即可
        }
    }

    // ---- SemCscFeature（CscFeature_* 配置读取） ----

    private fun hookSemCscFeature(cl: ClassLoader) {
        if (!ConfigHolder.current(prefs).spoofSemCscFeature) return
        runCatching {
            val clazz = Class.forName(CscConstants.CLASS_SEM_CSC_FEATURE, false, cl)
            safeHook(clazz, "getString", String::class.java) { chain -> cscFeatureIntercept(chain) }
            safeHook(clazz, "getString", String::class.java, String::class.java) { chain -> cscFeatureIntercept(chain) }
            safeHook(clazz, "getBoolean", String::class.java, Boolean::class.javaPrimitiveType!!) { chain ->
                cscFeatureBooleanIntercept(chain)
            }
        }.onFailure {
            // SemCscFeature 不存在则忽略
        }
    }

    private fun cscFeatureIntercept(chain: XposedInterface.Chain): Any {
        val key = chain.getArg(0) as? String ?: return chain.proceed()
        val cfg = ConfigHolder.current(prefs)
        return CscSpoof.matchCscFeature(key, cfg) ?: chain.proceed()
    }

    private fun cscFeatureBooleanIntercept(chain: XposedInterface.Chain): Any {
        val key = chain.getArg(0) as? String ?: return chain.proceed()
        val cfg = ConfigHolder.current(prefs)
        val value = CscSpoof.matchCscFeature(key, cfg) ?: return chain.proceed()
        return value.toBooleanStrictOrNull() ?: chain.proceed()
    }

    // ---------- hook 工具 ----------

    /**
     * 对类上的方法做 hook。方法不存在（不同 ROM/API 差异）时仅记日志，不影响其他 hook。
     */
    private fun safeHook(
        clazz: Class<*>,
        methodName: String,
        vararg paramTypes: Class<*>,
        interceptor: (XposedInterface.Chain) -> Any,
    ) {
        runCatching {
            val method = clazz.getMethod(methodName, *paramTypes)
            hook(method).intercept { chain ->
                interceptor(chain)
            }
        }.onFailure {
            log(Log.WARN, TAG, "CscChanger: hook $methodName failed: $it")
        }
    }

    /**
     * 配置持有者：节流式热更新。
     * 通过 [SharedPreferences]（LSPosed remote prefs）读取，每次 get 都由
     * LSPosed 服务转发到模块配置，修改后 2s 内自动感知，无需重启。
     */
    private object ConfigHolder {
        private const val RELOAD_INTERVAL_MS = 2000L

        @Volatile
        private var cached: CscConfig = CscConfig()

        @Volatile
        private var lastCheckElapsed = 0L

        @Synchronized
        fun current(prefs: SharedPreferences): CscConfig {
            val now = SystemClock.elapsedRealtime()
            if (now - lastCheckElapsed < RELOAD_INTERVAL_MS) return cached
            lastCheckElapsed = now
            cached = CscConfig.fromPrefs(prefs)
            return cached
        }
    }

    private companion object {
        const val TAG = "CscChanger"
    }
}
