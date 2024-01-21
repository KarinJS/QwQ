@file:OptIn(DelicateCoroutinesApi::class)
package moe.qwq.miko.internals.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mqq.app.MobileQQ

/**
 * 动态广播
 */
internal object DynamicReceiver: BroadcastReceiver() {
    const val MAIN_ACTION = "moe.qwq.main.dynamic"
    const val MSF_ACTION = "moe.qwq.msf.dynamic"

    private val cmdHandler = mutableMapOf<String, BroadcastRequest>()

    override fun onReceive(ctx: Context, intent: Intent) {
        GlobalScope.launch(Dispatchers.Default) {
            val cmd = intent.getStringExtra("__cmd") ?: ""
            try {
                if (cmd.isNotEmpty()) {
                    cmdHandler[cmd]?.callback?.handle(intent)
                }
            } catch (e: Throwable) {
                XposedBridge.log(e)
            }
        }
    }

    fun register(cmd: String, request: BroadcastRequest) {
        cmdHandler[cmd] = request
    }

    fun unregister(cmd: String) {
        cmdHandler.remove(cmd)
    }
}

fun Intent.broadcast() {
    MobileQQ.getContext().sendBroadcast(this)
}