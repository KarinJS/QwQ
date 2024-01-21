package moe.qwq.miko.actions

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.qwq.miko.ext.GlobalUi
import moe.qwq.miko.internals.broadcasts.BroadcastRequest
import moe.qwq.miko.internals.broadcasts.DynamicReceiver
import moe.qwq.miko.internals.receiver.MsgRecall
import moe.qwq.miko.tools.PlatformTools
import mqq.app.MobileQQ

class Broadcast: IAction {
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun invoke(ctx: Context) {
        kotlin.runCatching {
            MobileQQ.getMobileQQ().unregisterReceiver(DynamicReceiver)
        }
        GlobalUi = Handler(ctx.mainLooper)

        if (PlatformTools.isMainProcess()) {
            GlobalUi = Handler(ctx.mainLooper)
            GlobalScope.launch {
                val intentFilter = IntentFilter()
                intentFilter.addAction(DynamicReceiver.MAIN_ACTION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    MobileQQ.getMobileQQ().registerReceiver(
                        DynamicReceiver, intentFilter,
                        Context.RECEIVER_EXPORTED
                    )
                } else {
                    MobileQQ.getMobileQQ().registerReceiver(DynamicReceiver, intentFilter)
                }

                mainReceivers.forEach {
                    DynamicReceiver.register(it.getCommand(), BroadcastRequest { intent ->
                        it.onReceive(MobileQQ.getContext(), intent)
                    })
                }

                XposedBridge.log("[QwQ] Register Main::Broadcast successfully.")
            }
        } else if (PlatformTools.isMsfProcess()) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(DynamicReceiver.MSF_ACTION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                MobileQQ.getMobileQQ().registerReceiver(
                    DynamicReceiver, intentFilter,
                    Context.RECEIVER_EXPORTED
                )
            } else {
                MobileQQ.getMobileQQ().registerReceiver(DynamicReceiver, intentFilter)
            }
        }
    }

    companion object {
        private val mainReceivers = arrayOf(
            MsgRecall
        )
    }
}