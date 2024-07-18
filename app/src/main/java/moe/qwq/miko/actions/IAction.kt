package moe.qwq.miko.actions

import android.content.Context
import de.robv.android.xposed.XposedBridge
import moe.qwq.miko.internals.setting.QwQSetting

enum class ActionProcess {
    MSF, MAIN, ALL
}

interface IAction {
    operator fun invoke(ctx: Context) {
        kotlin.runCatching {
            if (canRun()) onRun(ctx)
        }.onFailure {
            XposedBridge.log(it)
        }
    }

    fun onRun(ctx: Context)

    fun canRun(): Boolean {
        val setting by QwQSetting.getSetting<Boolean>(name)
        return setting
    }

    val name: String

    val process: ActionProcess
        get() = ActionProcess.MAIN
}