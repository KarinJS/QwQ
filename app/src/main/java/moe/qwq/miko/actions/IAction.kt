package moe.qwq.miko.actions

import android.content.Context

enum class ActionProcess {
    MSF, MAIN, ALL
}

interface IAction {
    operator fun invoke(ctx: Context)

    val process: ActionProcess
        get() = ActionProcess.MAIN
}