package moe.qwq.miko

import android.content.Context
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.Broadcast
import moe.qwq.miko.internals.hooks.BrowserAccessRestrictions
import moe.qwq.miko.actions.FetchService
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.actions.PacketHijacker
import moe.qwq.miko.actions.WebJsBridge
import moe.qwq.miko.internals.hooks.HotUpdateSoPatch
import moe.qwq.miko.internals.hooks.SimplifyHomepageSidebar
import moe.qwq.miko.internals.hooks.TrashPacketHijacker

object ActionManager {
    private val FIRST_ACTION = arrayOf(
        Broadcast::class.java,
        WebJsBridge::class.java,
        FetchService::class.java,
        PacketHijacker::class.java,

        BrowserAccessRestrictions::class.java,
        SimplifyHomepageSidebar::class.java,
        TrashPacketHijacker::class.java,
        HotUpdateSoPatch::class.java,
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