package com.tv.utils.shell

import com.niki.cmd.RootShell
import com.niki.cmd.Shell
import com.niki.cmd.ShizukuShell
import com.niki.cmd.UserShell
import com.zephyr.global_values.TAG
import com.zephyr.global_values.globalContext
import com.zephyr.log.logD

class ShellManager {

    private val executors = listOf<Shell>(
        RootShell(),
        ShizukuShell(globalContext!!),
        UserShell()
    )

    private var selectedExecutor: Shell? = null
    private val selectedName: String
        get() = selectedExecutor?.javaClass?.simpleName ?: "no_executor"

    private suspend fun selectExecutor() {
        selectedExecutor = executors.firstOrNull {
            it.isAvailable(20_000L)
        }
        logD(TAG, "working as: $selectedName")
    }

    suspend fun exec(command: String): Map<String, Any?> {
        if (selectedExecutor == null)
            selectExecutor()
        val executor = selectedExecutor ?: throw Throwable("call select but no executor was chosen")
        if (!executor.isAvailable())
            throw Throwable("$selectedName is unavailable!")
        return executor.exec(command).run {
            mapOf(
                "exit_code" to exitCode,
                "output" to output
            )
        }
    }
}