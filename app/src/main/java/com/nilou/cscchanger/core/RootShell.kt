package com.nilou.cscchanger.core

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Root shell 执行工具（su 通道）。
 * 用于一键清理 Galaxy Store 数据等需要 root 的操作。
 */
object RootShell {

    /** 执行 su 命令，返回 (exitCode, stdout) */
    fun exec(command: String): Pair<Int, String> = runCatching {
        val process = Runtime.getRuntime().exec("su")
        val output = DataOutputStream(process.outputStream)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        output.writeBytes("$command\n")
        output.writeBytes("exit\n")
        output.flush()
        val stdout = reader.readText()
        process.waitFor()
        process.exitValue() to stdout
    }.getOrDefault(-1 to "root unavailable")

    /** 检查 root 是否可用 */
    fun hasRoot(): Boolean {
        val (code, _) = exec("id")
        return code == 0
    }
}
