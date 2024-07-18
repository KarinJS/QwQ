package moe.qwq.miko.actions

import android.content.Context
import de.robv.android.xposed.XposedBridge
import moe.qwq.miko.internals.setting.QwQSetting

enum class ActionProcess {
    MSF, MAIN, ALL
}

// 总是可以运行的Action
abstract class AlwaysRunAction : IAction {
    override val name: String = ""

    override fun canRun(): Boolean = true
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