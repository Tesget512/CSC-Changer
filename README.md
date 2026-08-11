# CSC Changer

一个 LSPosed 模块，通过 hook 三星系统框架的属性读取路径，让 Galaxy Store、主题商店等三星系统应用识别到你指定的区域（CSC / 销售代码）。

> 仅在 One UI 8.5 上测试通过，其他版本不保证能够使用。

## 特性

- 地区伪装：选择一个 CSC（如 TGY / BRI / KOO），自动填充地区码
- SIM 伪装：同时改写 SIM 运营商代码与地区代码（Galaxy Store 区域由 CSC + SIM MCC + 账号地区共同决定，只改 CSC 不够）
- 机型伪装：内置主流机型库（Galaxy Z / Flip / S 系列），修改 `ro.product.model` 等 Build 属性
- 快捷清理：一键清除 Galaxy Store 数据（需 root），清除区域缓存后立即生效
- 中英文界面

## 原理

三星系统应用读取区域信息主要走以下入口：

- `android.os.SystemProperties.get("ro.csc.sales_code")`
- `android.os.SemSystemProperties.get(...)`（三星私有扩展）
- `com.samsung.android.feature.SemCscFeature.getString("CscFeature_...")`
- `android.telephony.TelephonyManager`（SIM 运营商 / 地区代码）

模块 hook 这些读取路径，返回你配置的目标值。

需要说明的是：**Galaxy Store 的区域由 CSC + SIM MCC/MNC + 三星账号地区共同决定**。账号地区由账号服务器下发，本地无法修改；模块能改的是 CSC 属性与 SIM 信息，两者配合可覆盖绝大多数场景。

模块基于 libxposed 现代 API（102），配置通过 LSPosed 服务跨进程读写，不依赖 XSharedPreferences。

## 要求

- 三星设备（One UI 系统）
- 已安装 LSPosed（zygisk 版）且支持 libxposed API 102
- 部分功能（清理商店数据）需要 root

## 构建

需要 JDK 17+ 与 Android SDK（compileSdk 36）。

```bash
./gradlew :app:assembleRelease
```

APK 输出在 `app/build/outputs/apk/release/`。

签名从 `local.properties` 读取（`signing.storeFile` 等字段），未配置时回退到 debug keystore，便于直接安装测试。

## 使用

1. 安装 APK，在 LSPosed 管理器中启用模块
2. 勾选作用域：Galaxy Store（`com.sec.android.app.samsungapps`）、三星账号（`com.osp.app.signin`），以及需要改区域的系统应用
3. 重启（首次安装模块后需重启一次）
4. 打开应用，选择目标地区，开启 SIM 伪装，保存
5. 清理 Galaxy Store 数据，重新打开 Galaxy Store

## 免责声明

本模块修改系统属性，仅用于个人测试与学习。改动可能导致应用行为异常或影响系统稳定性，请自行评估风险。CSC 区域与账号服务条款相关的内容请以三星官方说明为准。
