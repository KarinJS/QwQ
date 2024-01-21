package moe.qwq.miko

import android.content.Context
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.Broadcast
import moe.qwq.miko.actions.BrowserAccessRestrictions
import moe.qwq.miko.actions.FetchService
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.actions.PacketHijacker
import moe.qwq.miko.actions.WebJsBridge

object ActionManager {
    private val FIRST_ACTION = arrayOf(
        Broadcast::class.java,
        BrowserAccessRestrictions::class.java,
        WebJsBridge::class.java,
        FetchService::class.java,
        PacketHijacker::class.java,
    )

    private val instanceMap = hashMapOf<Class<*>, IAction>()

    fun runFirst(ctx: Context, proc: ActionProcess) {
        FIRST_ACTION.forEach {
            val action = instanceOf(it)
            if (proc == action.process || proc == ActionProcess.ALL) {
                action(ctx)
            }
        }
    }

    fun instanceOf(cls: Class<*>): IAction = instanceMap.getOrPut(cls) {
        cls.getConstructor().newInstance() as IAction
    }
}