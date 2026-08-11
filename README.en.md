# CSC Changer

An LSPosed module that hooks the property-read paths of Samsung system frameworks, so Samsung apps like Galaxy Store and Theme Store recognize the region (CSC / sales code) you specify.

> Only tested on One UI 8.5. Other versions are not guaranteed to work.

[中文说明](README.md)

## Features

- Region spoofing: pick a CSC (e.g. TGY / BRI / KOO), region code is filled automatically
- SIM spoofing: rewrite SIM operator and region codes (Galaxy Store region is decided by CSC + SIM MCC + account region; CSC alone is not enough)
- Device spoofing: built-in model library (Galaxy Z / Flip / S series), rewrites `ro.product.model` and other Build properties
- One-tap cleanup: clear Galaxy Store data with root access, region cache clears instantly
- UI in Chinese and English

## How It Works

Samsung apps read region info through these paths:

- `android.os.SystemProperties.get("ro.csc.sales_code")`
- `android.os.SemSystemProperties.get(...)` (Samsung private extension)
- `com.samsung.android.feature.SemCscFeature.getString("CscFeature_...")`
- `android.telephony.TelephonyManager` (SIM operator / region)

The module hooks these paths and returns the values you configured.

Note: Galaxy Store region is determined by CSC + SIM MCC/MNC + Samsung account region. The account region comes from the server side and cannot be changed locally. The module changes CSC properties and SIM info, which covers most cases.

Built on the libxposed modern API (102). Configuration is read and written through the LSPosed service, no XSharedPreferences involved.

## Requirements

- Samsung device with One UI
- LSPosed (zygisk) with libxposed API 102 support
- Root access for some features (clearing store data)

## Build

JDK 17+ and Android SDK (compileSdk 36) required.

```bash
./gradlew :app:assembleRelease
```

Output: `app/build/outputs/apk/release/`.

Signing config is read from `local.properties` (`signing.storeFile` etc.). Falls back to the debug keystore when not configured, so the app can be installed and tested directly.

## Usage

1. Install the APK and enable the module in LSPosed Manager
2. Check the scope: Galaxy Store (`com.sec.android.app.samsungapps`), Samsung account (`com.osp.app.signin`), and other system apps you want to change
3. Reboot (required once after first install)
4. Open the app, pick a target region, enable SIM spoofing, save
5. Clear Galaxy Store data and reopen Galaxy Store

## Disclaimer

This module modifies system properties. For personal testing and learning only. Changes may cause unexpected behavior or affect system stability. Use at your own risk.
