package moe.qwq.miko.actions

import android.content.Context

interface IAction {
    operator fun invoke(ctx: Context)
}