# Xposed / LSPosed
-keep class com.nilou.cscchanger.MainHook { *; }
-keep class de.robv.android.xposed.** { *; }
-dontwarn de.robv.android.xposed.**

# 反射访问的类/字段/方法
-keep class com.nilou.cscchanger.core.** { *; }
