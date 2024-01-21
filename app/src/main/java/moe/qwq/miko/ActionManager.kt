package moe.qwq.miko

import android.content.Context
import moe.qwq.miko.actions.Broadcast
import moe.qwq.miko.actions.BrowserAccessRestrictions
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.actions.PacketHijacker
import moe.qwq.miko.actions.WebJsBridge

object ActionManager {
    private val FIRST_ACTION = arrayOf(
        Broadcast::class.java,

        BrowserAccessRestrictions::class.java,
        WebJsBridge::class.java,

        PacketHijacker::class.java
    )

    private val instanceMap = hashMapOf<Class<*>, IAction>()

    fun runFirst(ctx: Context) {
        FIRST_ACTION.forEach {
            val action = it.getConstructor().newInstance() as IAction
            instanceMap[it] = action
            action(ctx)
        }
    }

    fun api(cls: Class<*>): IAction = instanceMap.getOrPut(cls) {
        cls.getConstructor().newInstance() as IAction
    }
}