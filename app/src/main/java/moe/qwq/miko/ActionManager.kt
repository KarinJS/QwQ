package moe.qwq.miko

import android.content.Context
import moe.qwq.miko.actions.Broadcast
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.actions.PacketHijacker

object ActionManager {
    private val FIRST_ACTION = arrayOf(
        Broadcast::class.java,
        PacketHijacker::class.java
    )

    fun runFirst(ctx: Context) {
        FIRST_ACTION.forEach {
            val action = it.getConstructor().newInstance() as IAction
            action(ctx)
        }
    }
}